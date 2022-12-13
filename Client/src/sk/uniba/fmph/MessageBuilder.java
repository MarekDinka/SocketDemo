package sk.uniba.fmph;

import java.util.Arrays;

public class MessageBuilder {
    private static final byte END_OF_MESSAGE = 4;

    public static class EXE {
        private static final byte ID = 33;

        public static class FileTransfer {
            private static final byte ID = 33;

            public static byte[] build() {
                return new byte[]{EXE.ID, FileTransfer.ID};
            }

            public static boolean equals(byte[] msg) {
                return Arrays.equals(msg, new byte[]{EXE.ID, FileTransfer.ID, END_OF_MESSAGE});
            }
        }

        public static class EndOfSegment {
            private static final byte ID = 34;

            public static byte[] build() {
                return new byte[]{EXE.ID, ID};
            }

            public static boolean equals(byte[] msg) {
                return Arrays.equals(msg, new byte[]{EXE.ID, ID, END_OF_MESSAGE});
            }
        }
    }

    public static class GUI {
        private static final byte ID = 34;
        public static byte[] build() {
            return new byte[]{ID};
        }

        public static boolean equals(byte[] msg) {
            return Arrays.equals(msg, new byte[]{ID, END_OF_MESSAGE});
        }
    }

    public static class Controller {
        private static final byte ID = 35;
        public static byte[] build() {
            return new byte[]{ID};
        }

        public static boolean equals(byte[] msg) {
            return Arrays.equals(msg, new byte[]{ID, END_OF_MESSAGE});
        }
    }

    public static class Exception {
        private static final byte ID = 36;
        public static byte[] build() {
            return new byte[]{ID};
        }

        public static boolean equals(byte[] msg) {
            return Arrays.equals(msg, new byte[]{ID, END_OF_MESSAGE});
        }
    }
}
