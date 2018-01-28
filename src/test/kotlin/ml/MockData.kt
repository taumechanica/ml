// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.test

object MockData {
    val twoValueTarget = """
        @rem comment

        @attribute id exclude
        @attribute x1 numeric
        @attribute y nominal target
        @attribute x2 nominal

        @data
        1;1.0;0;C
        2;1.6;1;B
        3;2.1;0;A
        4;0.0;1;B
        5;0.4;0;A
    """

    val threeValueTarget = """
        @rem comment

        @attribute id exclude
        @attribute x1 numeric
        @attribute x2 nominal
        @attribute y nominal target

        @data
        1,1.0,C,0
        2,1.6,B,1
        3,2.1,A,2
        4,0.0,B,1
        5,0.4,A,0
        6,1.6,C,0
    """
}
