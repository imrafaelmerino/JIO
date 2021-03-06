package jio.api;

import jio.JsObjExp;
import jsonvalues.JsInt;
import jsonvalues.JsObj;
import jsonvalues.JsStr;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


import static jio.api.Constants.*;

public class TestJsObjExp {

    @Test
    public void test_sequential_constructors() {
        Assertions.assertEquals(
                JsObj.of("a",
                         JsStr.of("a")
                        ),
                JsObjExp.seq("a",
                             A.map(JsStr::of)
                            )
                        .join()
                               );

        Assertions.assertEquals(
                JsObj.of("a",
                         JsStr.of("a"),
                         "b",
                         JsStr.of("b")
                        ),
                JsObjExp.seq("a", A.map(JsStr::of),
                             "b", B.map(JsStr::of)
                            )
                        .join()
                               );

        Assertions.assertEquals(JsObj.of("a",
                                         JsStr.of("a"),
                                         "b",
                                         JsStr.of("b"),
                                         "c",
                                         JsInt.of(1)
                                        ),
                                JsObjExp.seq("a",
                                             A.map(JsStr::of),
                                             "b",
                                             B.map(JsStr::of),
                                             "c",
                                             ONE.map(JsInt::of)
                                            )
                                        .join()
                               );


        Assertions.assertEquals(JsObj.of("a",
                                         JsStr.of("a"),
                                         "b",
                                         JsStr.of("b"),
                                         "c",
                                         JsInt.of(1),
                                         "d",
                                         JsInt.of(2)
                                        ),
                                JsObjExp.seq("a",
                                             A.map(JsStr::of),
                                             "b",
                                             B.map(JsStr::of),
                                             "c",
                                             ONE.map(JsInt::of),
                                             "d",
                                             TWO.map(JsInt::of)
                                            )
                                        .join()
                               );

        Assertions.assertEquals(JsObj.of("a",
                                         JsStr.of("a"),
                                         "b",
                                         JsStr.of("b"),
                                         "c",
                                         JsInt.of(1),
                                         "d",
                                         JsInt.of(2),
                                         "e",
                                         JsInt.of(3)
                                        ),
                                JsObjExp.seq("a",
                                             A.map(JsStr::of),
                                             "b",
                                             B.map(JsStr::of),
                                             "c",
                                             ONE.map(JsInt::of),
                                             "d",
                                             TWO.map(JsInt::of),
                                             "e",
                                             THREE.map(JsInt::of)
                                            )
                                        .join()
                               );

        Assertions.assertEquals(JsObj.of("a",
                                         JsStr.of("a"),
                                         "b",
                                         JsStr.of("b"),
                                         "c",
                                         JsInt.of(1),
                                         "d",
                                         JsInt.of(2),
                                         "e",
                                         JsInt.of(3),
                                         "f",
                                         JsInt.of(2)
                                        ),
                                JsObjExp.seq("a",
                                             A.map(JsStr::of),
                                             "b",
                                             B.map(JsStr::of),
                                             "c",
                                             ONE.map(JsInt::of),
                                             "d",
                                             TWO.map(JsInt::of),
                                             "e",
                                             THREE.map(JsInt::of),
                                             "f", TWO.map(JsInt::of)
                                            )
                                        .join()
                               );

        Assertions.assertEquals(JsObj.of("a",
                                         JsStr.of("a"),
                                         "b",
                                         JsStr.of("b"),
                                         "c",
                                         JsInt.of(1),
                                         "d",
                                         JsInt.of(2),
                                         "e",
                                         JsInt.of(3),
                                         "f",
                                         JsInt.of(2),
                                         "g",
                                         JsInt.of(2)
                                        ),
                                JsObjExp.seq("a",
                                             A.map(JsStr::of),
                                             "b",
                                             B.map(JsStr::of),
                                             "c",
                                             ONE.map(JsInt::of),
                                             "d",
                                             TWO.map(JsInt::of),
                                             "e",
                                             THREE.map(JsInt::of),
                                             "f", TWO.map(JsInt::of),
                                             "g", TWO.map(JsInt::of)
                                            )
                                        .join()
                               );

        Assertions.assertEquals(JsObj.of("a",
                                         JsStr.of("a"),
                                         "b",
                                         JsStr.of("b"),
                                         "c",
                                         JsInt.of(1),
                                         "d",
                                         JsInt.of(2),
                                         "e",
                                         JsInt.of(3),
                                         "f",
                                         JsInt.of(2),
                                         "g",
                                         JsInt.of(2),
                                         "h",
                                         JsInt.of(2)
                                        ),
                                JsObjExp.seq("a",
                                             A.map(JsStr::of),
                                             "b",
                                             B.map(JsStr::of),
                                             "c",
                                             ONE.map(JsInt::of),
                                             "d",
                                             TWO.map(JsInt::of),
                                             "e",
                                             THREE.map(JsInt::of),
                                             "f", TWO.map(JsInt::of),
                                             "g", TWO.map(JsInt::of),
                                             "h", TWO.map(JsInt::of)
                                            )
                                        .join()
                               );

        Assertions.assertEquals(JsObj.of("a",
                                         JsStr.of("a"),
                                         "b",
                                         JsStr.of("b"),
                                         "c",
                                         JsInt.of(1),
                                         "d",
                                         JsInt.of(2),
                                         "e",
                                         JsInt.of(3),
                                         "f",
                                         JsInt.of(2),
                                         "g",
                                         JsInt.of(2),
                                         "h",
                                         JsInt.of(2),
                                         "i",
                                         JsInt.of(2)
                                        ),
                                JsObjExp.seq("a",
                                             A.map(JsStr::of),
                                             "b",
                                             B.map(JsStr::of),
                                             "c",
                                             ONE.map(JsInt::of),
                                             "d",
                                             TWO.map(JsInt::of),
                                             "e",
                                             THREE.map(JsInt::of),
                                             "f", TWO.map(JsInt::of),
                                             "g", TWO.map(JsInt::of),
                                             "h", TWO.map(JsInt::of),
                                             "i", TWO.map(JsInt::of)
                                            )
                                        .join()
                               );

        Assertions.assertEquals(JsObj.of("a",
                                         JsStr.of("a"),
                                         "b",
                                         JsStr.of("b"),
                                         "c",
                                         JsInt.of(1),
                                         "d",
                                         JsInt.of(2),
                                         "e",
                                         JsInt.of(3),
                                         "f",
                                         JsInt.of(2),
                                         "g",
                                         JsInt.of(2),
                                         "h",
                                         JsInt.of(2),
                                         "i",
                                         JsInt.of(2),
                                         "j",
                                         JsInt.of(3)
                                        ),
                                JsObjExp.seq("a",
                                             A.map(JsStr::of),
                                             "b",
                                             B.map(JsStr::of),
                                             "c",
                                             ONE.map(JsInt::of),
                                             "d",
                                             TWO.map(JsInt::of),
                                             "e",
                                             THREE.map(JsInt::of),
                                             "f", TWO.map(JsInt::of),
                                             "g", TWO.map(JsInt::of),
                                             "h", TWO.map(JsInt::of),
                                             "i", TWO.map(JsInt::of),
                                             "j", THREE.map(JsInt::of)
                                            )
                                        .join()
                               );

        Assertions.assertEquals(JsObj.of("a",
                                         JsStr.of("a"),
                                         "b",
                                         JsStr.of("b"),
                                         "c",
                                         JsInt.of(1),
                                         "d",
                                         JsInt.of(2),
                                         "e",
                                         JsInt.of(3),
                                         "f",
                                         JsInt.of(2),
                                         "g",
                                         JsInt.of(2),
                                         "h",
                                         JsInt.of(2),
                                         "i",
                                         JsInt.of(2),
                                         "j",
                                         JsInt.of(3),
                                         "k",
                                         JsInt.of(3)
                                        ),
                                JsObjExp.seq("a",
                                             A.map(JsStr::of),
                                             "b",
                                             B.map(JsStr::of),
                                             "c",
                                             ONE.map(JsInt::of),
                                             "d",
                                             TWO.map(JsInt::of),
                                             "e",
                                             THREE.map(JsInt::of),
                                             "f", TWO.map(JsInt::of),
                                             "g", TWO.map(JsInt::of),
                                             "h", TWO.map(JsInt::of),
                                             "i", TWO.map(JsInt::of),
                                             "j", THREE.map(JsInt::of),
                                             "k", THREE.map(JsInt::of)
                                            )
                                        .join()
                               );

        Assertions.assertEquals(JsObj.of("a",
                                         JsStr.of("a"),
                                         "b",
                                         JsStr.of("b"),
                                         "c",
                                         JsInt.of(1),
                                         "d",
                                         JsInt.of(2),
                                         "e",
                                         JsInt.of(3),
                                         "f",
                                         JsInt.of(2),
                                         "g",
                                         JsInt.of(2),
                                         "h",
                                         JsInt.of(2),
                                         "i",
                                         JsInt.of(2),
                                         "j",
                                         JsInt.of(3),
                                         "k",
                                         JsInt.of(3),
                                         "l",
                                         JsInt.of(3)
                                        ),
                                JsObjExp.seq("a",
                                             A.map(JsStr::of),
                                             "b",
                                             B.map(JsStr::of),
                                             "c",
                                             ONE.map(JsInt::of),
                                             "d",
                                             TWO.map(JsInt::of),
                                             "e",
                                             THREE.map(JsInt::of),
                                             "f", TWO.map(JsInt::of),
                                             "g", TWO.map(JsInt::of),
                                             "h", TWO.map(JsInt::of),
                                             "i", TWO.map(JsInt::of),
                                             "j", THREE.map(JsInt::of),
                                             "k", THREE.map(JsInt::of),
                                             "l", THREE.map(JsInt::of)
                                            )
                                        .join()
                               );

        Assertions.assertEquals(JsObj.of("a",
                                         JsStr.of("a"),
                                         "b",
                                         JsStr.of("b"),
                                         "c",
                                         JsInt.of(1),
                                         "d",
                                         JsInt.of(2),
                                         "e",
                                         JsInt.of(3),
                                         "f",
                                         JsInt.of(2),
                                         "g",
                                         JsInt.of(2),
                                         "h",
                                         JsInt.of(2),
                                         "i",
                                         JsInt.of(2),
                                         "j",
                                         JsInt.of(3),
                                         "k",
                                         JsInt.of(3),
                                         "l",
                                         JsInt.of(3),
                                         "m",
                                         JsInt.of(3)
                                        ),
                                JsObjExp.seq("a",
                                             A.map(JsStr::of),
                                             "b",
                                             B.map(JsStr::of),
                                             "c",
                                             ONE.map(JsInt::of),
                                             "d",
                                             TWO.map(JsInt::of),
                                             "e",
                                             THREE.map(JsInt::of),
                                             "f", TWO.map(JsInt::of),
                                             "g", TWO.map(JsInt::of),
                                             "h", TWO.map(JsInt::of),
                                             "i", TWO.map(JsInt::of),
                                             "j", THREE.map(JsInt::of),
                                             "k", THREE.map(JsInt::of),
                                             "l", THREE.map(JsInt::of),
                                             "m", THREE.map(JsInt::of)
                                            )
                                        .join()
                               );

        Assertions.assertEquals(JsObj.of("a",
                                         JsStr.of("a"),
                                         "b",
                                         JsStr.of("b"),
                                         "c",
                                         JsInt.of(1),
                                         "d",
                                         JsInt.of(2),
                                         "e",
                                         JsInt.of(3),
                                         "f",
                                         JsInt.of(2),
                                         "g",
                                         JsInt.of(2),
                                         "h",
                                         JsInt.of(2),
                                         "i",
                                         JsInt.of(2),
                                         "j",
                                         JsInt.of(3),
                                         "k",
                                         JsInt.of(3),
                                         "l",
                                         JsInt.of(3),
                                         "m",
                                         JsInt.of(3),
                                         "n",
                                         JsInt.of(3)
                                        ),
                                JsObjExp.seq("a",
                                             A.map(JsStr::of),
                                             "b",
                                             B.map(JsStr::of),
                                             "c",
                                             ONE.map(JsInt::of),
                                             "d",
                                             TWO.map(JsInt::of),
                                             "e",
                                             THREE.map(JsInt::of),
                                             "f", TWO.map(JsInt::of),
                                             "g", TWO.map(JsInt::of),
                                             "h", TWO.map(JsInt::of),
                                             "i", TWO.map(JsInt::of),
                                             "j", THREE.map(JsInt::of),
                                             "k", THREE.map(JsInt::of),
                                             "l", THREE.map(JsInt::of),
                                             "m", THREE.map(JsInt::of),
                                             "n", THREE.map(JsInt::of)
                                            )
                                        .join()
                               );

        Assertions.assertEquals(JsObj.of("a",
                                         JsStr.of("a"),
                                         "b",
                                         JsStr.of("b"),
                                         "c",
                                         JsInt.of(1),
                                         "d",
                                         JsInt.of(2),
                                         "e",
                                         JsInt.of(3),
                                         "f",
                                         JsInt.of(2),
                                         "g",
                                         JsInt.of(2),
                                         "h",
                                         JsInt.of(2),
                                         "i",
                                         JsInt.of(2),
                                         "j",
                                         JsInt.of(3),
                                         "k",
                                         JsInt.of(3),
                                         "l",
                                         JsInt.of(3),
                                         "m",
                                         JsInt.of(3),
                                         "n",
                                         JsInt.of(3),
                                         "o",
                                         JsInt.of(3)
                                        ),
                                JsObjExp.seq("a",
                                             A.map(JsStr::of),
                                             "b",
                                             B.map(JsStr::of),
                                             "c",
                                             ONE.map(JsInt::of),
                                             "d",
                                             TWO.map(JsInt::of),
                                             "e",
                                             THREE.map(JsInt::of),
                                             "f", TWO.map(JsInt::of),
                                             "g", TWO.map(JsInt::of),
                                             "h", TWO.map(JsInt::of),
                                             "i", TWO.map(JsInt::of),
                                             "j", THREE.map(JsInt::of),
                                             "k", THREE.map(JsInt::of),
                                             "l", THREE.map(JsInt::of),
                                             "m", THREE.map(JsInt::of),
                                             "n", THREE.map(JsInt::of),
                                             "o", THREE.map(JsInt::of)
                                            )
                                        .join()
                               );
    }

    @Test
    public void test_parallel_constructors() {
        Assertions.assertEquals(
                JsObj.of("a",
                         JsStr.of("a")
                        ),
                JsObjExp.par("a",
                             A.map(JsStr::of)
                            )
                        .join()
                               );

        Assertions.assertEquals(
                JsObj.of("a",
                         JsStr.of("a"),
                         "b",
                         JsStr.of("b")
                        ),
                JsObjExp.par("a", A.map(JsStr::of),
                             "b", B.map(JsStr::of)
                            )
                        .join()
                               );

        Assertions.assertEquals(JsObj.of("a",
                                         JsStr.of("a"),
                                         "b",
                                         JsStr.of("b"),
                                         "c",
                                         JsInt.of(1)
                                        ),
                                JsObjExp.par("a",
                                             A.map(JsStr::of),
                                             "b",
                                             B.map(JsStr::of),
                                             "c",
                                             ONE.map(JsInt::of)
                                            )
                                        .join()
                               );


        Assertions.assertEquals(JsObj.of("a",
                                         JsStr.of("a"),
                                         "b",
                                         JsStr.of("b"),
                                         "c",
                                         JsInt.of(1),
                                         "d",
                                         JsInt.of(2)
                                        ),
                                JsObjExp.par("a",
                                             A.map(JsStr::of),
                                             "b",
                                             B.map(JsStr::of),
                                             "c",
                                             ONE.map(JsInt::of),
                                             "d",
                                             TWO.map(JsInt::of)
                                            )
                                        .join()
                               );

        Assertions.assertEquals(JsObj.of("a",
                                         JsStr.of("a"),
                                         "b",
                                         JsStr.of("b"),
                                         "c",
                                         JsInt.of(1),
                                         "d",
                                         JsInt.of(2),
                                         "e",
                                         JsInt.of(3)
                                        ),
                                JsObjExp.par("a",
                                             A.map(JsStr::of),
                                             "b",
                                             B.map(JsStr::of),
                                             "c",
                                             ONE.map(JsInt::of),
                                             "d",
                                             TWO.map(JsInt::of),
                                             "e",
                                             THREE.map(JsInt::of)
                                            )
                                        .join()
                               );

        Assertions.assertEquals(JsObj.of("a",
                                         JsStr.of("a"),
                                         "b",
                                         JsStr.of("b"),
                                         "c",
                                         JsInt.of(1),
                                         "d",
                                         JsInt.of(2),
                                         "e",
                                         JsInt.of(3),
                                         "f",
                                         JsInt.of(2)
                                        ),
                                JsObjExp.par("a",
                                             A.map(JsStr::of),
                                             "b",
                                             B.map(JsStr::of),
                                             "c",
                                             ONE.map(JsInt::of),
                                             "d",
                                             TWO.map(JsInt::of),
                                             "e",
                                             THREE.map(JsInt::of),
                                             "f", TWO.map(JsInt::of)
                                            )
                                        .join()
                               );

        Assertions.assertEquals(JsObj.of("a",
                                         JsStr.of("a"),
                                         "b",
                                         JsStr.of("b"),
                                         "c",
                                         JsInt.of(1),
                                         "d",
                                         JsInt.of(2),
                                         "e",
                                         JsInt.of(3),
                                         "f",
                                         JsInt.of(2),
                                         "g",
                                         JsInt.of(2)
                                        ),
                                JsObjExp.par("a",
                                             A.map(JsStr::of),
                                             "b",
                                             B.map(JsStr::of),
                                             "c",
                                             ONE.map(JsInt::of),
                                             "d",
                                             TWO.map(JsInt::of),
                                             "e",
                                             THREE.map(JsInt::of),
                                             "f", TWO.map(JsInt::of),
                                             "g", TWO.map(JsInt::of)
                                            )
                                        .join()
                               );

        Assertions.assertEquals(JsObj.of("a",
                                         JsStr.of("a"),
                                         "b",
                                         JsStr.of("b"),
                                         "c",
                                         JsInt.of(1),
                                         "d",
                                         JsInt.of(2),
                                         "e",
                                         JsInt.of(3),
                                         "f",
                                         JsInt.of(2),
                                         "g",
                                         JsInt.of(2),
                                         "h",
                                         JsInt.of(2)
                                        ),
                                JsObjExp.par("a",
                                             A.map(JsStr::of),
                                             "b",
                                             B.map(JsStr::of),
                                             "c",
                                             ONE.map(JsInt::of),
                                             "d",
                                             TWO.map(JsInt::of),
                                             "e",
                                             THREE.map(JsInt::of),
                                             "f", TWO.map(JsInt::of),
                                             "g", TWO.map(JsInt::of),
                                             "h", TWO.map(JsInt::of)
                                            )
                                        .join()
                               );

        Assertions.assertEquals(JsObj.of("a",
                                         JsStr.of("a"),
                                         "b",
                                         JsStr.of("b"),
                                         "c",
                                         JsInt.of(1),
                                         "d",
                                         JsInt.of(2),
                                         "e",
                                         JsInt.of(3),
                                         "f",
                                         JsInt.of(2),
                                         "g",
                                         JsInt.of(2),
                                         "h",
                                         JsInt.of(2),
                                         "i",
                                         JsInt.of(2)
                                        ),
                                JsObjExp.par("a",
                                             A.map(JsStr::of),
                                             "b",
                                             B.map(JsStr::of),
                                             "c",
                                             ONE.map(JsInt::of),
                                             "d",
                                             TWO.map(JsInt::of),
                                             "e",
                                             THREE.map(JsInt::of),
                                             "f", TWO.map(JsInt::of),
                                             "g", TWO.map(JsInt::of),
                                             "h", TWO.map(JsInt::of),
                                             "i", TWO.map(JsInt::of)
                                            )
                                        .join()
                               );

        Assertions.assertEquals(JsObj.of("a",
                                         JsStr.of("a"),
                                         "b",
                                         JsStr.of("b"),
                                         "c",
                                         JsInt.of(1),
                                         "d",
                                         JsInt.of(2),
                                         "e",
                                         JsInt.of(3),
                                         "f",
                                         JsInt.of(2),
                                         "g",
                                         JsInt.of(2),
                                         "h",
                                         JsInt.of(2),
                                         "i",
                                         JsInt.of(2),
                                         "j",
                                         JsInt.of(3)
                                        ),
                                JsObjExp.par("a",
                                             A.map(JsStr::of),
                                             "b",
                                             B.map(JsStr::of),
                                             "c",
                                             ONE.map(JsInt::of),
                                             "d",
                                             TWO.map(JsInt::of),
                                             "e",
                                             THREE.map(JsInt::of),
                                             "f", TWO.map(JsInt::of),
                                             "g", TWO.map(JsInt::of),
                                             "h", TWO.map(JsInt::of),
                                             "i", TWO.map(JsInt::of),
                                             "j", THREE.map(JsInt::of)
                                            )
                                        .join()
                               );

        Assertions.assertEquals(JsObj.of("a",
                                         JsStr.of("a"),
                                         "b",
                                         JsStr.of("b"),
                                         "c",
                                         JsInt.of(1),
                                         "d",
                                         JsInt.of(2),
                                         "e",
                                         JsInt.of(3),
                                         "f",
                                         JsInt.of(2),
                                         "g",
                                         JsInt.of(2),
                                         "h",
                                         JsInt.of(2),
                                         "i",
                                         JsInt.of(2),
                                         "j",
                                         JsInt.of(3),
                                         "k",
                                         JsInt.of(3)
                                        ),
                                JsObjExp.par("a",
                                             A.map(JsStr::of),
                                             "b",
                                             B.map(JsStr::of),
                                             "c",
                                             ONE.map(JsInt::of),
                                             "d",
                                             TWO.map(JsInt::of),
                                             "e",
                                             THREE.map(JsInt::of),
                                             "f", TWO.map(JsInt::of),
                                             "g", TWO.map(JsInt::of),
                                             "h", TWO.map(JsInt::of),
                                             "i", TWO.map(JsInt::of),
                                             "j", THREE.map(JsInt::of),
                                             "k", THREE.map(JsInt::of)
                                            )
                                        .join()
                               );

        Assertions.assertEquals(JsObj.of("a",
                                         JsStr.of("a"),
                                         "b",
                                         JsStr.of("b"),
                                         "c",
                                         JsInt.of(1),
                                         "d",
                                         JsInt.of(2),
                                         "e",
                                         JsInt.of(3),
                                         "f",
                                         JsInt.of(2),
                                         "g",
                                         JsInt.of(2),
                                         "h",
                                         JsInt.of(2),
                                         "i",
                                         JsInt.of(2),
                                         "j",
                                         JsInt.of(3),
                                         "k",
                                         JsInt.of(3),
                                         "l",
                                         JsInt.of(3)
                                        ),
                                JsObjExp.par("a",
                                             A.map(JsStr::of),
                                             "b",
                                             B.map(JsStr::of),
                                             "c",
                                             ONE.map(JsInt::of),
                                             "d",
                                             TWO.map(JsInt::of),
                                             "e",
                                             THREE.map(JsInt::of),
                                             "f", TWO.map(JsInt::of),
                                             "g", TWO.map(JsInt::of),
                                             "h", TWO.map(JsInt::of),
                                             "i", TWO.map(JsInt::of),
                                             "j", THREE.map(JsInt::of),
                                             "k", THREE.map(JsInt::of),
                                             "l", THREE.map(JsInt::of)
                                            )
                                        .join()
                               );

        Assertions.assertEquals(JsObj.of("a",
                                         JsStr.of("a"),
                                         "b",
                                         JsStr.of("b"),
                                         "c",
                                         JsInt.of(1),
                                         "d",
                                         JsInt.of(2),
                                         "e",
                                         JsInt.of(3),
                                         "f",
                                         JsInt.of(2),
                                         "g",
                                         JsInt.of(2),
                                         "h",
                                         JsInt.of(2),
                                         "i",
                                         JsInt.of(2),
                                         "j",
                                         JsInt.of(3),
                                         "k",
                                         JsInt.of(3),
                                         "l",
                                         JsInt.of(3),
                                         "m",
                                         JsInt.of(3)
                                        ),
                                JsObjExp.par("a",
                                             A.map(JsStr::of),
                                             "b",
                                             B.map(JsStr::of),
                                             "c",
                                             ONE.map(JsInt::of),
                                             "d",
                                             TWO.map(JsInt::of),
                                             "e",
                                             THREE.map(JsInt::of),
                                             "f", TWO.map(JsInt::of),
                                             "g", TWO.map(JsInt::of),
                                             "h", TWO.map(JsInt::of),
                                             "i", TWO.map(JsInt::of),
                                             "j", THREE.map(JsInt::of),
                                             "k", THREE.map(JsInt::of),
                                             "l", THREE.map(JsInt::of),
                                             "m", THREE.map(JsInt::of)
                                            )
                                        .join()
                               );

        Assertions.assertEquals(JsObj.of("a",
                                         JsStr.of("a"),
                                         "b",
                                         JsStr.of("b"),
                                         "c",
                                         JsInt.of(1),
                                         "d",
                                         JsInt.of(2),
                                         "e",
                                         JsInt.of(3),
                                         "f",
                                         JsInt.of(2),
                                         "g",
                                         JsInt.of(2),
                                         "h",
                                         JsInt.of(2),
                                         "i",
                                         JsInt.of(2),
                                         "j",
                                         JsInt.of(3),
                                         "k",
                                         JsInt.of(3),
                                         "l",
                                         JsInt.of(3),
                                         "m",
                                         JsInt.of(3),
                                         "n",
                                         JsInt.of(3)
                                        ),
                                JsObjExp.par("a",
                                             A.map(JsStr::of),
                                             "b",
                                             B.map(JsStr::of),
                                             "c",
                                             ONE.map(JsInt::of),
                                             "d",
                                             TWO.map(JsInt::of),
                                             "e",
                                             THREE.map(JsInt::of),
                                             "f", TWO.map(JsInt::of),
                                             "g", TWO.map(JsInt::of),
                                             "h", TWO.map(JsInt::of),
                                             "i", TWO.map(JsInt::of),
                                             "j", THREE.map(JsInt::of),
                                             "k", THREE.map(JsInt::of),
                                             "l", THREE.map(JsInt::of),
                                             "m", THREE.map(JsInt::of),
                                             "n", THREE.map(JsInt::of)
                                            )
                                        .join()
                               );

        Assertions.assertEquals(JsObj.of("a",
                                         JsStr.of("a"),
                                         "b",
                                         JsStr.of("b"),
                                         "c",
                                         JsInt.of(1),
                                         "d",
                                         JsInt.of(2),
                                         "e",
                                         JsInt.of(3),
                                         "f",
                                         JsInt.of(2),
                                         "g",
                                         JsInt.of(2),
                                         "h",
                                         JsInt.of(2),
                                         "i",
                                         JsInt.of(2),
                                         "j",
                                         JsInt.of(3),
                                         "k",
                                         JsInt.of(3),
                                         "l",
                                         JsInt.of(3),
                                         "m",
                                         JsInt.of(3),
                                         "n",
                                         JsInt.of(3),
                                         "o",
                                         JsInt.of(3)
                                        ),
                                JsObjExp.par("a",
                                             A.map(JsStr::of),
                                             "b",
                                             B.map(JsStr::of),
                                             "c",
                                             ONE.map(JsInt::of),
                                             "d",
                                             TWO.map(JsInt::of),
                                             "e",
                                             THREE.map(JsInt::of),
                                             "f", TWO.map(JsInt::of),
                                             "g", TWO.map(JsInt::of),
                                             "h", TWO.map(JsInt::of),
                                             "i", TWO.map(JsInt::of),
                                             "j", THREE.map(JsInt::of),
                                             "k", THREE.map(JsInt::of),
                                             "l", THREE.map(JsInt::of),
                                             "m", THREE.map(JsInt::of),
                                             "n", THREE.map(JsInt::of),
                                             "o", THREE.map(JsInt::of)
                                            )
                                        .join()
                               );
    }


}
