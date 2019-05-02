package com.aevi.barcode.scanner;

public class Tuple {

    public static <T1> Tuple1<T1> of(T1 t1) {
        return new Tuple1<T1>(t1);
    }

    public static <T1, T2> Tuple2<T1, T2> of(T1 t1, T2 t2) {
        return new Tuple2<T1, T2>(t1, t2);
    }

    public static <T1, T2, T3> Tuple3<T1, T2, T3> of(T1 t1, T2 t2, T3 t3) {
        return new Tuple3<T1, T2, T3>(t1, t2, t3);
    }

    public static class Tuple1<T1> {
        public final T1 t1;

        public Tuple1(T1 t1) {
            this.t1 = t1;
        }
    }

    public static class Tuple2<T1, T2> {
        public final T1 t1;
        public final T2 t2;

        public Tuple2(T1 t1, T2 t2) {
            this.t1 = t1;
            this.t2 = t2;
        }
    }

    public static class Tuple3<T1, T2, T3> {
        public final T1 t1;
        public final T2 t2;
        public final T3 t3;

        public Tuple3(T1 t1, T2 t2, T3 t3) {
            this.t1 = t1;
            this.t2 = t2;
            this.t3 = t3;
        }
    }
}
