package org.jjophoven.input;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.util.Set;

public class Keybinds {
    public void apply(Gamepad g, Set<Integer> keys) {
        Gamepad changes = new Gamepad();
        changes.dpad_up = keys.contains(Keys.W);
        changes.dpad_down = keys.contains(Keys.S);
        changes.dpad_left = keys.contains(Keys.A);
        changes.dpad_right = keys.contains(Keys.D);

        changes.a = keys.contains(Keys.ENTER);

        changes.right_stick_x =
                keys.contains(Keys.Q) && keys.contains(Keys.E) ? 0 :
                        keys.contains(Keys.Q) ? -1 :
                        keys.contains(Keys.E) ? 1 : 0;

        // TODO more keybinds

        g.fromByteArray(changes.toByteArray());
    }
}
