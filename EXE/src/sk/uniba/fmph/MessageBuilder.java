package sk.uniba.fmph;

import java.util.Arrays;

public class MessageBuilder {
    private static final byte END_OF_MESSAGE = 4;

    public static class EXE {
        private static final byte ID = 'a';

        public static class FileTransfer {
            private static final byte ID = 'a';

            public static byte[] build() {
                return new byte[]{EXE.ID, FileTransfer.ID};
            }

            public static boolean equals(byte[] msg) {
                return Arrays.equals(msg, new byte[]{EXE.ID, FileTransfer.ID});
            }
        }

        public static class EndOfSegment {
            private static final byte ID = 'b';

            public static byte[] build() {
                return new byte[]{EXE.ID, ID};
            }

            public static boolean equals(byte[] msg) {
                return Arrays.equals(msg, new byte[]{EXE.ID, ID});
            }
        }
    }

    public static class GUI {
        private static final byte ID = 'b';
        public static byte[] build() {
            return new byte[]{ID};
        }

        public static class Exception {
            private static final byte ID = 'a';

            public static byte[] build() {
                return new byte[]{GUI.ID, ID};
            }

            public static boolean equals(byte[] msg) {
                return Arrays.equals(msg, new byte[]{GUI.ID, ID});
            }
        }

        public static boolean equals(byte[] msg) {
            return Arrays.equals(msg, new byte[]{ID});
        }
    }

    public static class Controller {
        private static final byte ID = 'c';
        public static byte[] build() {
            return new byte[]{ID};
        }

        public static boolean equals(byte[] msg) {
            return Arrays.equals(msg, new byte[]{ID});
        }
    }
}
