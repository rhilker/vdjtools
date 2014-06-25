/**
 Copyright 2014 Mikhail Shugay (mikhail.shugay@gmail.com)

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.antigenomics.vdjtools.intersection

class IntersectionResult {
    final int clones1, clones2, clones12
    final double freq1, freq2, freq12

    IntersectionResult(int clones1, int clones2, int clones12,
                     double freq1, double freq2, double freq12) {
        this.clones1 = clones1
        this.clones2 = clones2
        this.clones12 = clones12
        this.freq1 = freq1
        this.freq2 = freq2
        this.freq12 = freq12
    }

    final static String HEADER = "clones1\tclones2\tclones12\tfreq1\tfreq2\tfreq12"

    @Override
    String toString() {
        [clones1, clones2, clones12, freq1, freq2, freq12].join("\t")
    }
}
