package Benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Fork(value = 1)
@Warmup(iterations = 4)
@Measurement(iterations = 2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)

public class Compare {
    @State(Scope.Thread)
    public static class CompareBenchmarkState {
        public final long seed = 0;
        public final int stringSize = 10000;
        public final int suffixSize = 100;
        public final int prefixSize = 100;
        public final int subSize = 128;
        public final String[] randomStrings = new String[10000];
        private final Random rand = new Random(seed);
        public String targetSuffix;
        public String targetPrefix;
        public String targetSub;
        public String makeRandomString() {
            char[] chars = new char[stringSize];
            for (int i = 0; i < stringSize; i++) {
                chars[i] = (char) ((32 + rand.nextInt()) % 127);
            }
            return new String(chars);
        }
        @Setup
        public void setup() {
            int randomSize = randomStrings.length;
            for (int i = 0; i < randomSize; i++) {
                randomStrings[i] = makeRandomString();
            }
            targetSuffix = randomStrings[randomSize - 1].substring(stringSize - 1 - suffixSize);
            targetPrefix = randomStrings[randomSize - 1].substring(0, prefixSize);
            targetSub = randomStrings[randomSize - 1].substring(stringSize/2, stringSize/2 + subSize);
        }
    }

    @Benchmark
    public void sort(CompareBenchmarkState state, Blackhole bh) {
        Arrays.sort(state.randomStrings);
        bh.consume(state.randomStrings);
    }

    @Benchmark
    public void indexOf(CompareBenchmarkState state, Blackhole bh) {
        int result = 0;
        for (int i = 0; i < state.randomStrings.length; i++) {
            result += state.randomStrings[i].indexOf(state.targetSub);
        }
        bh.consume(result);
    }

    @Benchmark
    public void startsWith(CompareBenchmarkState state, Blackhole bh) {
        boolean result = false;
        for (int i = 0; i < state.randomStrings.length; i++) {
            result ^= state.randomStrings[i].startsWith(state.targetPrefix);
        }
        bh.consume(result);
    }

    @Benchmark
    public void endsWith(CompareBenchmarkState state, Blackhole bh) {
        boolean result = false;
        for (int i = 0; i < state.randomStrings.length; i++) {
            result ^= state.randomStrings[i].endsWith(state.targetSuffix);
        }
        bh.consume(result);
    }

    @Benchmark
    public void compareTo(CompareBenchmarkState state, Blackhole bh) {
        int result = 0;
        for (int i = 0; i < state.randomStrings.length; i++) {
            result += state.randomStrings[0].compareTo(state.randomStrings[i]);
        }
        bh.consume(result);
    }
}
