package polly.java.qrcodetool.view;

import java.awt.Toolkit;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {
	public MainFrame() {
		super("QRCodeTool - By Pollyman");
		
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/assets/AppIcon.png")));
		setContentPane(new MainPanel(this));
		setSize(900, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void makeReadyAndShow() {
		setLocationRelativeTo(null);
		setVisible(true);
	}
}
