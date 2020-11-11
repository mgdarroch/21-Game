import javax.swing.*;
import java.awt.*;

public class OtherPlayerHandPanel extends JPanel implements HandPanel {

    private static final Color BACKGROUND_COLOUR = new Color(47, 79, 79);
    private static final Color TEXT_COLOUR = new Color(230, 230, 230);
    private JPanel cardsPanel;
    private JLabel playerIDLabel;
    private JLabel handMessageLabel;
    private final int panelPlayerID;
    private final boolean isDealer;

    public OtherPlayerHandPanel(int playerID, boolean isDealer) {
        this.panelPlayerID = playerID;
        this.isDealer = isDealer;
        setupPanel();
    }

    private void setupPanel() {
        setBackground(BACKGROUND_COLOUR);
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        cardsPanel = new JPanel();
        cardsPanel.setBackground(BACKGROUND_COLOUR);
        constraints.gridx = 0;
        constraints.gridy = 0;
        add(cardsPanel, constraints);
        playerIDLabel = new JLabel();
        if (isDealer) {
            playerIDLabel = new JLabel("Player " + panelPlayerID + ": DEALER");
        } else {
            playerIDLabel = new JLabel("Player " + panelPlayerID);
        }
        playerIDLabel.setForeground(TEXT_COLOUR);
        constraints.gridy = 1;
        add(playerIDLabel, constraints);
        handMessageLabel = new JLabel();
        handMessageLabel.setForeground(TEXT_COLOUR);
        constraints.gridy = 2;
        add(handMessageLabel, constraints);
    }

    public void setHandMessageLabel(String handMessageLabel) {
        this.handMessageLabel.setText(handMessageLabel);
    }

    @Override
    public int getPanelPlayerID() {
        return panelPlayerID;
    }

    @Override
    public void bust() {
        setHandMessageLabel("You busted.");
        showChanges();
    }

    private void showChanges() {
        revalidate();
        repaint();
        setVisible(true);
    }

    @Override
    public void turnError() {

    }

    @Override
    public void enableHitStand() {

    }

    @Override
    public void addCard(JLabel cardLabel) {
        cardsPanel.add(cardLabel);
        showChanges();
    }

    public void clearCardPanel(){
        cardsPanel.removeAll();
        showChanges();
    }

    @Override
    public void setHandValueLabel(String handValue) {
    }
}
