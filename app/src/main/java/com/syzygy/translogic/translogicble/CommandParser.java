package com.syzygy.translogic.translogicble;

import java.util.Arrays;

public class CommandParser {
    enum Command {
        PARAMETERS, MEASUREMENT, UNKNOWN;
        private String value;

        public void setValue(String value) {
            this.value = value;
        }

        public String getValue() {
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
        if (bytes.length >= 8) {
            command = Command.PARAMETERS;
            command.setValue(new String(Arrays.copyOfRange(bytes, 3, 7)));
        } else if (bytes.length >= 5) {
            command = Command.MEASUREMENT;
            command.setValue(new String(Arrays.copyOfRange(bytes, 3, 5)));
        } else command = Command.UNKNOWN;
        return command;
    }
}
