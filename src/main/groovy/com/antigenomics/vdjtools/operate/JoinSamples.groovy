/*
 * Copyright 2013-2015 Mikhail Shugay (mikhail.shugay@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.antigenomics.vdjtools.operate

import com.antigenomics.vdjtools.Software
import com.antigenomics.vdjtools.io.SampleWriter
import com.antigenomics.vdjtools.join.JointSample
import com.antigenomics.vdjtools.join.OccurenceJoinFilter
import com.antigenomics.vdjtools.overlap.OverlapType
import com.antigenomics.vdjtools.sample.Sample
import com.antigenomics.vdjtools.sample.SampleCollection

import static com.antigenomics.vdjtools.util.ExecUtil.formOutputPath
import static com.antigenomics.vdjtools.util.RUtil.asNumeric
import static com.antigenomics.vdjtools.util.RUtil.execute

def I_TYPE_DEFAULT = "aa", DEFAULT_TIMES_DETECTED = "2"
def cli = new CliBuilder(usage: "JoinSamples [options] " +
        "[sample1 sample2 sample3 ... if -m is not specified] output_prefix")
cli.h("display help message")

// General

cli.m(longOpt: "metadata", argName: "filename", args: 1,
        "Metadata file. First and second columns should contain file name and sample id. " +
                "Header is mandatory and will be used to assign column names for metadata." +
                "If column named 'time' is present, it will be used to specify time point sequence.")
cli.i(longOpt: "intersect-type", argName: "string", args: 1,
        "Intersection rule to apply. Allowed values: $OverlapType.allowedNames. " +
                "Will use '$I_TYPE_DEFAULT' by default.")
// Tracking and output

cli.x(longOpt: "times-detected", argName: "int", args: 1,
        "Minimal number of samples where clonotype should be detected. " +
                "[default = $DEFAULT_TIMES_DETECTED]")
cli.c(longOpt: "compress", "Compress output sample files.")

// Plotting

cli._(longOpt: "plot-type", argName: "<pdf|png>", args: 1, "Plot output format [default=pdf]")
cli.p(longOpt: "plot", "[plotting] Turns on plotting.")

def opt = cli.parse(args)

if (opt == null) {
    System.exit(-1)
}

if (opt.h) {
    cli.usage()
    System.exit(0)
}

// Check if metadata is provided

def metadataFileName = opt.m

if (metadataFileName ? opt.arguments().size() != 1 : opt.arguments().size() < 4) {
    if (metadataFileName)
        println "Only output prefix should be provided in case of -m"
    else
        println "At least 3 sample files should be provided if not using -m"
    cli.usage()
    System.exit(-1)
}

// Other parameters

def timesDetected = (opt.x ?: DEFAULT_TIMES_DETECTED).toInteger(),
    plot = (boolean) opt.p,
    outputPrefix = opt.arguments()[-1],
    compress = (boolean) opt.c,
    plotType = (opt.'plot-type' ?: "pdf").toString()

def scriptName = getClass().canonicalName.split("\\.")[-1]

// Select overlap type

def iName = opt.i ?: I_TYPE_DEFAULT
def intersectionType = OverlapType.getByShortName(iName)

if (!intersectionType) {
    println "[ERROR] Bad overlap type specified ($iName). " +
            "Allowed values are: $OverlapType.allowedNames"
    System.exit(-1)
}

//
// Batch load samples
//

println "[${new Date()} $scriptName] Reading in all samples"

def sampleCollection = metadataFileName ?
        new SampleCollection((String) metadataFileName, Software.VDJtools, true, false) :
        new SampleCollection(opt.arguments()[0..-2], Software.VDJtools, true, false)

if (sampleCollection.size() < 3) {
    println "[ERROR] Metadata file should contain at least 3 samples"
    System.exit(-1)
}

println "[${new Date()} $scriptName] ${sampleCollection.size()} samples loaded"

//
// Join samples
//

println "[${new Date()} $scriptName] Joining samples by ${intersectionType}"

def jointSample = new JointSample(intersectionType, sampleCollection.collect() as Sample[],
        new OccurenceJoinFilter(timesDetected))

//
// Write output tables
//

println "[${new Date()} $scriptName] Writing tabular output"

def sampleWriter = new SampleWriter(compress)
sampleWriter.write(jointSample, formOutputPath(outputPrefix, "join", intersectionType.shortName, "table"))

//
// Write summary output
//
def div = (0..<sampleCollection.size()).collectEntries {
    [((it + 1).toString()): sampleCollection[it].diversity] // toString is crucial here, remember GString != String
}
def freqMap = new HashMap<String, Integer>()

println "[${new Date()} $scriptName] Enumerating clonotype occurrences"

jointSample.each { clonotype ->
    def mask = (0..<sampleCollection.size()).collect { i -> clonotype.present(i) ? (i + 1) : "" }.join("")
    freqMap.put(mask, (freqMap[mask] ?: 0) + 1)
}

new File(formOutputPath(outputPrefix, "join", intersectionType.shortName, "summary")).withPrintWriter { pw ->
    pw.println(sampleCollection.collect { it.sampleMetadata.sampleId }.join("\t") + "\tclonotypes")
    sampleIds = (1..sampleCollection.size()).collect { it.toString() }
    div.each { entry ->
        pw.println(
                sampleIds.collect {
                    it == entry.key ? 1 : 0
                }.join("\t") + "\t" + entry.value
        )
    }
    freqMap.sort { -it.value }.each { entry ->
        pw.println(
                sampleIds.collect {
                    entry.key.contains(it) ? 1 : 0
                }.join("\t") + "\t" + entry.value
        )
    }
}

// Plot Venn diagram
if (plot) {
    def countArea = { String sample ->
        asNumeric(div[sample])
    }

    def countFreq = { String mask ->
        asNumeric(freqMap[mask] ?: 0)
    }


    println "[${new Date()} $scriptName] Plotting"

    // looks overly redundant, but that's how input arguments are passed in VennDiagram package
    execute("join_venn.r",
            countArea("1"), countArea("2"), countArea("3"), countArea("4"), countArea("5"),
            countFreq("12"), countFreq("13"), countFreq("14"),
            countFreq("15"), countFreq("23"), countFreq("24"),
            countFreq("25"), countFreq("34"), countFreq("35"),
            countFreq("45"),
            countFreq("123"), countFreq("124"), countFreq("125"),
            countFreq("134"), countFreq("135"), countFreq("145"),
            countFreq("234"), countFreq("235"), countFreq("245"),
            countFreq("345"),
            countFreq("1234"), countFreq("1235"), countFreq("1245"),
            countFreq("1345"), countFreq("2345"),
            countFreq("12345"),
            sampleCollection.collect { it.sampleMetadata.sampleId.replaceAll(/ +/, ".") }[0..4].join(","),
            formOutputPath(outputPrefix, "join", intersectionType.shortName, "venn", plotType)
    )
}

println "[${new Date()} $scriptName] Finished"