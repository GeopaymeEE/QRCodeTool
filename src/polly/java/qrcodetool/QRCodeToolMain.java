package polly.java.qrcodetool;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import polly.java.qrcodetool.view.MainFrame;

public class QRCodeToolMain {
	private static void createAndShowGUI() {
		MainFrame mf = new MainFrame();
		mf.makeReadyAndShow();
	}
	
	public static void main(String[] args) {
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				createAndShowGUI();
			}
		});
	}
}
