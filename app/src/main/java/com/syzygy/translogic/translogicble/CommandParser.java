package com.syzygy.translogic.translogicble;

import java.util.Arrays;

public class CommandParser {
    enum Command {
        PRESSURE, DEPTH, UNKNOWN;
        private Double value;

        public void setValue(Double value) {
            this.value = value;
        }

        public Double getValue() {
            return value;
        }
    }

    public static byte[] setDepthMeasurementSystemMMCommand() {
        return new byte[]{(byte) (0xa2 & 0xff), 'U', 'T', 'M', (byte) (0xa7 & 0xff)};
    }

    public static byte[] setPressureMeasurementSystemMMCommand() {
        return new byte[]{(byte) (0xa2 & 0xff), 'U', 'P', 'P', (byte) (0xa7 & 0xff)};
    }

    public static byte[] getCurrentMeasurementCommand() {
        return new byte[]{(byte) (0xa2 & 0xff), 'U', (byte) (0xa7 & 0xff)};
    }

    public static byte[] getDepthValueCommand() {
        return new byte[]{(byte) (0xa2 & 0xff), 'T', (byte) (0xa7 & 0xff)};
    }

    public static byte[] getPressureValueCommand() {
        return new byte[]{(byte) (0xa2 & 0xff), 'P', (byte) (0xa7 & 0xff)};
    }

    public static Command parseValue(byte[] bytes) {
        Command command;
        if (bytes != null && bytes.length >= 7
                && bytes[0] == (byte) (0xa2 & 0xff) && bytes[bytes.length - 1] == (byte) (0xa7 & 0xff)) {
            switch (bytes[1]) {
                case 'P':
                    command = Command.PRESSURE;
                    break;
                case 'T':
                    command = Command.DEPTH;
                    break;
                default:
                    return Command.UNKNOWN;
            }
            command.setValue(Double.parseDouble(new String(Arrays.copyOfRange(bytes, 2, bytes.length - 1))) / 100);
            return command;
        }
        return Command.UNKNOWN;
    }
}
