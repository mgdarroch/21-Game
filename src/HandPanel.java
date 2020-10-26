import javax.swing.*;

public interface HandPanel {

    /**
     * Interface for the HandPanels.
     * There are two hand panels.  One for the Client's player and one for the Other players.  The PlayerHandPanel has Hit and Stand buttons where the OtherPlayerHandPanel does not.
     * @return
     */

    int getPanelPlayerID();

    void bust();

    void turnError();

    void enableHitStand();

    void addCard(JLabel cardImageLabel);

    void setHandValueLabel(String serverMessageComponent);

    void setHandMessageLabel(String s);
}
