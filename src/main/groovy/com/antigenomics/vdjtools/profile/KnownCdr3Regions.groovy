/*
 * Copyright (c) 2015, Bolotin Dmitry, Chudakov Dmitry, Shugay Mikhail
 * (here and after addressed as Inventors)
 * All Rights Reserved
 *
 * Permission to use, copy, modify and distribute any part of this program for
 * educational, research and non-profit purposes, by non-profit institutions
 * only, without fee, and without a written agreement is hereby granted,
 * provided that the above copyright notice, this paragraph and the following
 * three paragraphs appear in all copies.
 *
 * Those desiring to incorporate this work into commercial products or use for
 * commercial purposes should contact the Inventors using one of the following
 * email addresses: chudakovdm@mail.ru, chudakovdm@gmail.com
 *
 * IN NO EVENT SHALL THE INVENTORS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE INVENTORS HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE INVENTORS HAS
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS. THE INVENTORS MAKES NO REPRESENTATIONS AND EXTENDS NO
 * WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY
 * PATENT, TRADEMARK OR OTHER RIGHTS.
 */

package com.antigenomics.vdjtools.profile

class KnownCdr3Regions implements Iterable<Cdr3Region> {
    private final Map<String, Cdr3Region> regions

    public static final KnownCdr3Regions INSTANCE = new KnownCdr3Regions()

    private KnownCdr3Regions() {
        this.regions = [new VGermline(), new DGermline(), new JGermline(),
                        new VDJunction(), new DJJunction(),
                        new VJJunction(), new FullCdr3(),
                        new Cdr3Center()].collectEntries {
            [(it.name): it]
        }
    }

    public Cdr3Region getByName(String name) {
        if (!regions.containsKey(name)) {
            throw new IllegalArgumentException("[ERROR] Unknown region $name, allowed values are: ${regions.keySet()}")
        }
        regions[name]
    }

    public List<String> getRegionNames() {
        regions.keySet() as List
    }

    @Override
    Iterator<Cdr3Region> iterator() {
        regions.values().iterator()
    }
}
