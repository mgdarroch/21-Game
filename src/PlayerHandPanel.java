import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PlayerHandPanel extends JPanel implements ActionListener, HandPanel {

    private static final Color BACKGROUND_COLOUR = new Color(47, 79, 79);
    private static final Color TEXT_COLOUR = new Color(230, 230, 230);
    private static final Dimension BUTTONS_DIMENSION = new Dimension(110, 25);
    private final Client controller; // client GUI controller
    private JPanel cardsPanel;
    private JLabel playerIDLabel;
    private JLabel handValueLabel;
    private JLabel handMessageLabel;
    private JButton hitButton;
    private JButton standButton;
    private final int panelPlayerID;
    private final boolean isDealer;

    public PlayerHandPanel(Client controller, int playerID, boolean isDealer) {
        this.controller = controller;
        this.panelPlayerID = playerID;
        this.isDealer = isDealer;
        setupPanel();
        setupActionListeners();
    }

    /**
     * Sets up the Player's hand panel.  The players hand panel also includes Hit and Stand buttons.  The OtherPlayersPanel does not.
     */
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
        handValueLabel = new JLabel();
        handValueLabel.setForeground(TEXT_COLOUR);
        constraints.gridy = 2;
        add(handValueLabel, constraints);
        handMessageLabel = new JLabel();
        handMessageLabel.setForeground(TEXT_COLOUR);
        constraints.gridy = 3;
        add(handMessageLabel, constraints);
        JPanel hitStandButtonsPanel = new JPanel();
        hitStandButtonsPanel.setBackground(BACKGROUND_COLOUR);
        hitButton = new JButton("Hit");
        hitButton.setPreferredSize(BUTTONS_DIMENSION);
        hitButton.setEnabled(false);
        hitButton.setVisible(false);
        standButton = new JButton("Stand");
        standButton.setPreferredSize(BUTTONS_DIMENSION);
        standButton.setEnabled(false);
        standButton.setVisible(false);
        hitStandButtonsPanel.add(hitButton);
        hitStandButtonsPanel.add(standButton);
        constraints.gridy = 4;
        add(hitStandButtonsPanel, constraints);
    }

    /**
     * Adds the actions listeners
     */
    private void setupActionListeners() {
        hitButton.addActionListener(this);
        standButton.addActionListener(this);
    }


    /**
     * Displays View changes.
     */
    public void showChanges() {
        revalidate();
        repaint();
        setVisible(true);
    }

    /**
     * Turn option error.
     */
    public void turnError() {
        setHandMessageLabel("ERROR");
        showChanges();
    }

    /**
     * Enables the hit and stand buttons.
     */
    public void enableHitStand() {
        enableHitButton(true);
        enableStandButton(true);
        showChanges();
    }

    private void enableHitButton(Boolean b) {
        hitButton.setEnabled(b);
        hitButton.setVisible(b);
        showChanges();
    }

    private void enableStandButton(Boolean b) {
        standButton.setEnabled(b);
        standButton.setVisible(b);
        showChanges();
    }

    /**
     * Adds a card object to the panel
     * @param cardLabel
     */
    public void addCard(JLabel cardLabel) {
        cardsPanel.add(cardLabel);
        showChanges();
    }


    /**
     * Sets the player's label to show he has busted.
     */
    public void bust() {
        setHandMessageLabel("You busted.");
        showChanges();
    }



    @Override
    public void actionPerformed(ActionEvent e) {
        Object target = e.getSource();
        if (target == hitButton) {
            controller.sendClientMessage(hitButton.getText());
        } else if (target == standButton) {
            controller.sendClientMessage(standButton.getText());
        }

        enableHitButton(false);
        enableStandButton(false);
    }

    //Getters and Setters

    public int getPanelPlayerID() {
        return panelPlayerID;
    }

    public void setHandValueLabel(String handValue) {
        handValueLabel.setText("Hand Value: " + handValue);
        showChanges();
    }

    public void setHandMessageLabel(String message) {
        handMessageLabel.setText(message);
        showChanges();
    }
}

