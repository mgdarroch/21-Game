import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ClientView extends JFrame implements ActionListener {

    private static final Dimension FRAME_MINIMUM_DIMENSION = new Dimension(960, 800);
    private static final Dimension PLAYER_HANDS_PANEL_DIMENSION = new Dimension(930, 365);
    private static final Dimension BUTTONS_DIMENSION = new Dimension(110, 25);
    private static final Color BACKGROUND_COLOUR = new Color(47, 79, 79);
    private static final Color TEXT_COLOUR = new Color(230, 230, 230);
    private static final Float WELCOME_LABEL_SIZE = 24.0f;
    private static final Float HANDS_LABEL_SIZE = 14.0f;
    private final Client controller;

    private JLabel welcomeWaitingLabel;

    private JPanel playerHandsPanel;
    private JPanel otherPlayerHandsPanel;
    private JPanel clientPlayerPanel;
    private JLabel messageLabel;
    private JButton yesButton;
    private JButton noButton;
    private JLabel twentyOneLabel;
    private JPanel buttonPanel;
    private JLabel turnMoneyLabel;
    private JLabel turnWaitingLabel;

    private JLabel continuePlayingMessageLabel;
    private JLabel continuePlayingMoneyLabel;
    private JLabel continuePlayingWaitingLabel;

    public ClientView(Client controller) {
        this.controller = controller;
        setupWindowListener(this.controller);
        setupFrame();
        createPanels();
        setupActionListeners();
    }

    /**
     * Sets up the window listener for the quit option.  This method is not working properly. Quitting does not remove the player from the game.  Please wait until the end of the round and select No when prompted to quit.
     * @param controller
     */
    private void setupWindowListener(Client controller) {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?", null, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    controller.quitGame();
                    System.exit(0);
                }
            }
        });
    }

    private void setupFrame() {
        setTitle("Twenty One Game");
        setMinimumSize(FRAME_MINIMUM_DIMENSION);
        setResizable(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new CardLayout());
        showChanges();
    }

    private void createPanels() {
        createWelcomePanel();
        createTurnPanel();
        createContinuePlayingPanel();
    }

    private void setupActionListeners() {
        yesButton.addActionListener(this);
        noButton.addActionListener(this);
    }

    private void showChanges() {
        revalidate();
        repaint();
        setVisible(true);
    }

    private void createWelcomePanel() {
        JPanel welcomePanel = new JPanel(new GridBagLayout());
        welcomePanel.setBackground(BACKGROUND_COLOUR);
        GridBagConstraints constraints = new GridBagConstraints();
        JLabel welcomeLabel = new JLabel("Twenty One Game");
        welcomeLabel.setForeground(TEXT_COLOUR);
        welcomeLabel.setFont(welcomeLabel.getFont().deriveFont(WELCOME_LABEL_SIZE));
        constraints.gridx = 0;
        constraints.gridy = 0;
        welcomePanel.add(welcomeLabel, constraints);
        welcomeWaitingLabel = new JLabel("Waiting for other players to join.");
        welcomeWaitingLabel.setForeground(TEXT_COLOUR);
        welcomeWaitingLabel.setVisible(false);
        constraints.gridy = 1;
        welcomePanel.add(welcomeWaitingLabel, constraints);
        add(welcomePanel, PanelNames.WELCOMEPANEL.toString());
    }

    public void setWelcomeWaiting(Boolean b) {
        welcomeWaitingLabel.setVisible(b);
        showChanges();
    }

    private void createTurnPanel() {
        JPanel turnPanel = new JPanel(new GridBagLayout());
        turnPanel.setBackground(BACKGROUND_COLOUR);
        GridBagConstraints constraints = new GridBagConstraints();
        JLabel twentyOneTitle = new JLabel("Player's Hand");
        twentyOneTitle.setForeground(TEXT_COLOUR);
        twentyOneTitle.setFont(twentyOneTitle.getFont().deriveFont(HANDS_LABEL_SIZE));
        constraints.gridx = 0;
        constraints.gridy = 0;
        turnPanel.add(twentyOneTitle, constraints);
        playerHandsPanel = new JPanel();
        playerHandsPanel.setBackground(BACKGROUND_COLOUR);
        playerHandsPanel.setLayout(new GridLayout(3, 1));
        JScrollPane playerHandsScrollPane = new JScrollPane(playerHandsPanel);
        playerHandsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        playerHandsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        playerHandsScrollPane.setPreferredSize(PLAYER_HANDS_PANEL_DIMENSION);
        playerHandsScrollPane.setBorder(BorderFactory.createEmptyBorder());
        constraints.gridy = 1;
        constraints.fill = MAXIMIZED_VERT;
        turnPanel.add(playerHandsScrollPane, constraints);
        clientPlayerPanel = new JPanel();
        clientPlayerPanel.setBackground(BACKGROUND_COLOUR);
        playerHandsPanel.add(clientPlayerPanel);
        otherPlayerHandsPanel = new JPanel();
        otherPlayerHandsPanel.setBackground(BACKGROUND_COLOUR);
        playerHandsPanel.add(otherPlayerHandsPanel);
        messageLabel = new JLabel();
        messageLabel.setForeground(TEXT_COLOUR);
        constraints.gridy = 2;
        turnPanel.add(messageLabel, constraints);
        buttonPanel = new JPanel();
        yesButton = new JButton("Yes");
        yesButton.setPreferredSize(BUTTONS_DIMENSION);
        enableYesButton(false);
        buttonPanel.add(yesButton);
        buttonPanel.setBackground(BACKGROUND_COLOUR);
        noButton = new JButton("No");
        noButton.setPreferredSize(BUTTONS_DIMENSION);
        enableNoButton(false);
        buttonPanel.add(noButton);
        constraints.gridy = 3;
        turnPanel.add(buttonPanel, constraints);
        twentyOneLabel = new JLabel();
        twentyOneLabel.setForeground(TEXT_COLOUR);
        constraints.gridy = 4;
        turnPanel.add(twentyOneLabel, constraints);
        turnMoneyLabel = new JLabel();
        turnMoneyLabel.setForeground(TEXT_COLOUR);
        constraints.gridy = 5;
        turnPanel.add(turnMoneyLabel, constraints);
        turnWaitingLabel = new JLabel("Waiting for other players to take their turns.");
        turnWaitingLabel.setForeground(TEXT_COLOUR);
        turnWaitingLabel.setVisible(false);
        constraints.gridy = 6;
        turnPanel.add(turnWaitingLabel, constraints);
        add(turnPanel, PanelNames.TURNPANEL.toString());
    }

    public void addPlayerHandPanel(OtherPlayerHandPanel playerHandPanel) {
        otherPlayerHandsPanel.add(playerHandPanel);
        showChanges();
    }

    public void addClientHandPanel(PlayerHandPanel clientHandPanel) {
        clientPlayerPanel.add(clientHandPanel);
        showChanges();
    }

    public void removePlayerHandPanel(HandPanel playerHandPanel) {
        playerHandsPanel.remove((JPanel) playerHandPanel);
        showChanges();
    }

    public void setTurnMoneyLabel(String money) {
        turnMoneyLabel.setText("Stakes: " + money);
        showChanges();
    }

    public void setMessageLabel(String message) {
        messageLabel.setText(message);
        showChanges();
    }

    public void removeInsuranceBetInfo() {
        messageLabel.setText("");
        showChanges();
    }

    public void enableContinuePlaying() {
        setMessageLabel("Would you like to keep playing?");
        enableYesButton(true);
        enableNoButton(true);
        showChanges();
    }

    public void continuePlayingError() {
        setMessageLabel("ERROR");
        enableYesButton(true);
        enableNoButton(true);
        showChanges();
    }

    public void setTwentyOneLabel(String message) {
        twentyOneLabel.setText(message);
        showChanges();
    }

    public void setTurnWaiting(Boolean b) {
        turnWaitingLabel.setVisible(b);
        showChanges();
    }

    private void createContinuePlayingPanel() {
        JPanel continuePlayingPanel = new JPanel(new GridBagLayout());
        continuePlayingPanel.setBackground(BACKGROUND_COLOUR);
        GridBagConstraints constraints = new GridBagConstraints();
        continuePlayingMessageLabel = new JLabel();
        continuePlayingMessageLabel.setForeground(TEXT_COLOUR);
        constraints.gridx = 0;
        constraints.gridy = 0;
        continuePlayingPanel.add(continuePlayingMessageLabel, constraints);
        JPanel continuePlayingButtonsPanel = new JPanel();
        continuePlayingButtonsPanel.setBackground(BACKGROUND_COLOUR);
        constraints.gridy = 1;
        continuePlayingPanel.add(continuePlayingButtonsPanel, constraints);
        continuePlayingMoneyLabel = new JLabel();
        continuePlayingMoneyLabel.setForeground(TEXT_COLOUR);
        constraints.gridy = 2;
        continuePlayingPanel.add(continuePlayingMoneyLabel, constraints);
        continuePlayingWaitingLabel = new JLabel("Waiting for other players to connect.");
        continuePlayingWaitingLabel.setForeground(TEXT_COLOUR);
        continuePlayingWaitingLabel.setVisible(false);
        constraints.gridy = 3;
        continuePlayingPanel.add(continuePlayingWaitingLabel, constraints);
        add(continuePlayingPanel, PanelNames.CONTINUEPLAYINGPANEL.toString());
    }

    public void setContinuePlayingMoneyLabel(String money) {
        continuePlayingMoneyLabel.setText("Stakes: " + money);
        showChanges();
    }

    public void gameOver() {
        continuePlayingMessageLabel.setText("Thanks for playing!");
        showChanges();
    }

    public void setContinuePlayingWaiting(Boolean b) {
        continuePlayingWaitingLabel.setVisible(b);
        showChanges();
    }


    private void enableYesButton(Boolean b) {
        yesButton.setEnabled(b);
        yesButton.setVisible(b);
        showChanges();
    }

    private void enableNoButton(Boolean b) {
        noButton.setEnabled(b);
        noButton.setVisible(b);
        showChanges();
    }

    private void showPanel(PanelNames panelName) {
        CardLayout cardLayout = (CardLayout) getContentPane().getLayout();
        cardLayout.show(getContentPane(), panelName.toString());
        showChanges();
    }

    public void showWelcomePanel() {
        showPanel(PanelNames.WELCOMEPANEL);
        setTitle("Twenty One Game: Player " + controller.getPlayerID() + "'s Client");
    }

    public void showBetPanel() {
        showPanel(PanelNames.BETPANEL);
    }

    public void showTurnPanel() {
        showPanel(PanelNames.TURNPANEL);
    }

    public void showContinuePlayingPanel() {
        showPanel(PanelNames.CONTINUEPLAYINGPANEL);
    }

    public void reset() {
        createPanels();
        setupActionListeners();
        showContinuePlayingPanel();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object target = e.getSource();
        if (target == yesButton) {
            controller.sendClientMessage(yesButton.getText());
            enableYesButton(false);
            enableNoButton(false);
        } else if (target == noButton) {
            controller.sendClientMessage(noButton.getText());
            enableYesButton(false);
            enableNoButton(false);
        }
    }

    private enum PanelNames {
        WELCOMEPANEL, BETPANEL, TURNPANEL, CONTINUEPLAYINGPANEL
    }
}




