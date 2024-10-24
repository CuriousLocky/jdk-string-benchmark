package Benchmark;

import MyStrings.ArrayRope;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Fork(value = 1)
@Warmup(iterations = 4)
@Measurement(iterations = 2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class ConcatSub {
    @State(Scope.Thread)
    public static class ConcatSubState {
        private final int concatIter = 1000;
        private final int subIter = 1000;
        private final int stringSize = 1000;
        public final int subStringSize = 100;
        public final boolean checkResult = false;
        private final int seed = 0;
        private final Random rand = new Random(seed);
        public String str1;
        public String str2;
        public String result;
        public final int[] subStringsIndices = new int[subIter];
        @Setup
        public void setup() {
            for (int i = 0; i < concatIter; i++) {
                subStringsIndices[i] = i;
            }
            str1 = makeRandomAsciiString(stringSize);
            str2 = makeRandomAsciiString(stringSize);
            StringBuilder sb = new StringBuilder(str1);
            for (int i = 0; i < concatIter; i++) {
                sb.append(str2);
            }
            StringBuilder resultBuilder = new StringBuilder();
            for (int i : subStringsIndices) {
                resultBuilder.append(sb.subSequence(i, i + subStringSize));
            }
            result = resultBuilder.toString();
        }
        private String makeRandomAsciiString(int stringSize) {
            char[] chars = new char[stringSize];
            for (int i = 0; i < stringSize; i++) {
                chars[i] = (char) ((32 + rand.nextInt()) % 127);
            }
            return new String(chars);
        }
    }

    @Benchmark
    public void stringBuffer(ConcatSubState state, Blackhole bh) {
        StringBuilder srcBuilder = new StringBuilder(state.str1);
        srcBuilder.append(String.valueOf(state.str2).repeat(state.concatIter));
        StringBuilder tgtBuilder = new StringBuilder();
        for (int i : state.subStringsIndices) {
            tgtBuilder.append(srcBuilder.subSequence(i, i + state.subStringSize));
        }
        String result = tgtBuilder.toString();
        bh.consume(result);
    }

    @Benchmark
    public void arrayRope(ConcatSubState state, Blackhole bh) {
        ArrayRope rope = new ArrayRope(state.str1);
        for (int i = 0; i < state.concatIter; i++) {
            rope.append(state.str2);
        }
        ArrayRope rope2 = new ArrayRope();
        for (int i : state.subStringsIndices) {
            rope2.append(rope.subSequence(i, i + state.subStringSize));
        }
        String result = rope2.toString();
        bh.consume(result);
//        assert !state.checkResult || result.equals(state.result);
//        194us
    }

    @Benchmark
    public void arrayRopeConcat(ConcatSubState state, Blackhole bh) {
        ArrayRope rope = new ArrayRope(state.str1);
        for (int i = 0; i < state.concatIter; i++) {
            rope.append(state.str2);
        }
        ArrayRope rope2 = new ArrayRope();
        for (int i : state.subStringsIndices) {
            rope2 = rope2.concat(rope.subSequence(i, i + state.subStringSize));
        }
        String result = rope2.toString();
        bh.consume(result);
//        assert !state.checkResult || result.equals(state.result);
    }

    @Benchmark
    public void arrayRopeNoMat(ConcatSubState state, Blackhole bh) {
        ArrayRope rope = new ArrayRope(state.str1);
        for (int i = 0; i < state.concatIter; i++) {
            rope.append(state.str2);
        }
        ArrayRope rope2 = new ArrayRope();
        for (int i : state.subStringsIndices) {
            rope2.append(rope.subSequence(i, i + state.subStringSize));
        }
        bh.consume(rope2);
//        assert !state.checkResult || result.equals(state.result);
    }
}
