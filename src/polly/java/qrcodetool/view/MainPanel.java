package polly.java.qrcodetool.view;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import org.apache.commons.codec.binary.Base64;

import polly.java.qrcodetool.qrcode.QRCode;
import polly.java.util.ImageUtil;
import polly.java.util.LogUtil;

@SuppressWarnings("serial")
public class MainPanel extends JPanel {
	
	private static final String AUTHOR_EMAIL = "polly.abo@gmail.com";
	private static final String PROJECT_URI = "https://github.com/pollyman/QRCodeTool";
	
	private static final int DEFAULT_ENCODE_IMAGE_WIDTH = 200;
	private static final int DEFAULT_ENCODE_IMAGE_HEIGHT = 200;
	private static final String DEFAULT_SAVE_IMAGE_FORMAT = "png";
	
	private JFrame owner;
	
	private JLabel imgPath;
	private JTextField imgPathText;
	private JButton imgSelectBtn;
	private JLabel imgIcon;
	private JTextArea imgInfoText;
	private JButton decodeBtn;
	private JButton encodeBtn;
	private JTextField imgWidthText;
	private JTextField imgHeightText;
	private JButton b64DecodeBtn;
	private JButton b64EncodeBtn;
	
	private JPopupMenu imgIconPopupMenu;
	private JMenuItem pasteMenuItem;
	private JMenuItem copyMenuItem;
	private JMenuItem saveMenuItem;
	
	private SortedMap<String,Charset> availableCharsets;
	private JComboBox<String> charsetBox;
	
	private JLabel authorLabel;
	
	private JFileChooser fc;
	
	private String currImgPath;
	private BufferedImage currImg;
	
	private boolean isLoading = false;
	
	public MainPanel(JFrame owner) {
		super(new BorderLayout());
		this.owner = owner;
		
		JPanel northPanel = new JPanel(new BorderLayout());
		add(northPanel, BorderLayout.NORTH);
		
		imgPath = new JLabel("图片地址：");
		northPanel.add(imgPath, BorderLayout.WEST);
		
		imgPathText = new JTextField("在此输入图片URL或者点击右边“本地选择”按钮从您的电脑中选择一张图片");
		imgPathText.setToolTipText(imgPathText.getText());
		northPanel.add(imgPathText, BorderLayout.CENTER);
		imgPathText.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				loadAndShowImage(false);
			}
		});
		
		imgSelectBtn = new JButton("本地选择");
		northPanel.add(imgSelectBtn, BorderLayout.EAST);
		imgSelectBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				doSelectImg();
			}
		});
		
		JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 20));
		add(centerPanel, BorderLayout.CENTER);
		
		imgIcon = new JLabel("可将图片拖放到此处或者右键选择“粘贴图片”");
		imgIcon.setToolTipText(imgIcon.getText());
		imgIcon.setHorizontalAlignment(SwingConstants.CENTER);
		centerPanel.add(imgIcon);
		imgIcon.setBorder(BorderFactory.createTitledBorder("二维码图像"));
		imgIcon.addComponentListener(new ComponentAdapter() {
			
			public void componentResized(ComponentEvent e) {
				fitImage();
			}
		});
		imgIcon.setTransferHandler(new TransferHandler() {
			
			public boolean canImport(TransferHandler.TransferSupport info) {
				if (info.isDataFlavorSupported(DataFlavor.stringFlavor) 
						|| info.isDataFlavorSupported(DataFlavor.javaFileListFlavor) 
								|| info.isDataFlavorSupported(DataFlavor.imageFlavor)) {
					return true;
				}
				return false;
			}
			
			public boolean importData(TransferHandler.TransferSupport info) {
				if (!info.isDrop()) {
					return false;
				}
				
				boolean success = false;
				Transferable t = info.getTransferable();
				if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					try {
						String imagePath = (String) t.getTransferData(DataFlavor.stringFlavor);
						imgPathText.setText(imagePath);
						loadAndShowImage(false);
						success = true;
					} catch (Exception e) {
						LogUtil.log(this, e.getMessage());
					}
				}
				if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					try {
						@SuppressWarnings("unchecked")
						List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
						String imagePath = files.get(0).getPath();
						imgPathText.setText(imagePath);
						loadAndShowImage(false);
						success = true;
					} catch (Exception e) {
						LogUtil.log(this, e.getMessage());
					}
				}
				if (t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
					try {
						BufferedImage image = (BufferedImage) t.getTransferData(DataFlavor.imageFlavor);
						showImage(image);
						imgPathText.setText("");
						currImgPath = "";
						success = true;
					} catch (Exception e) {
						LogUtil.log(this, e.getMessage());
					}
				}
				
				return success;
			}
		});
		
		imgIconPopupMenu = new JPopupMenu();
		pasteMenuItem = imgIconPopupMenu.add(new AbstractAction("粘贴图片") {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				pasteImage();
			}
		});
		imgIconPopupMenu.addSeparator();
		copyMenuItem = imgIconPopupMenu.add(new AbstractAction("复制图片") {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				copyImage();
			}
		});
		saveMenuItem = imgIconPopupMenu.add(new AbstractAction("保存为" + DEFAULT_SAVE_IMAGE_FORMAT + "图片...") {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				saveImage();
			}
		});
		imgIcon.addMouseListener(new MouseAdapter() {
			
			public void mouseReleased(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					pasteMenuItem.setEnabled(canPasteImage());
					copyMenuItem.setEnabled(currImg != null);
					saveMenuItem.setEnabled(currImg != null);
					imgIconPopupMenu.show(imgIcon, e.getX(), e.getY());
				}
			}
		});
		
		imgInfoText = new JTextArea();
		imgInfoText.setLineWrap(true);
		imgInfoText.setWrapStyleWord(true);
		JScrollPane imgInfoTextView = new JScrollPane(imgInfoText);
		centerPanel.add(imgInfoTextView);
		imgInfoTextView.setBorder(BorderFactory.createTitledBorder("二维码信息"));
		
		JPanel southPanel = new JPanel(new BorderLayout());
		add(southPanel, BorderLayout.SOUTH);
		
		authorLabel = new JLabel("<html><a href='" + PROJECT_URI + "'>" + AUTHOR_EMAIL + "</a></html>");
		southPanel.add(authorLabel, BorderLayout.WEST);
		authorLabel.setToolTipText(PROJECT_URI);
		authorLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		authorLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		authorLabel.addMouseListener(new MouseAdapter() {
			
			public void mouseClicked(MouseEvent e) {
				if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
					try {
						Desktop.getDesktop().browse(URI.create(PROJECT_URI));
					} catch (IOException ioE) {
						ioE.printStackTrace();
					}
				}
			}
		});

		JPanel southCPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		southPanel.add(southCPanel, BorderLayout.CENTER);
		
		availableCharsets = Charset.availableCharsets();
		Set<String> keySet = availableCharsets.keySet();
		String[] keys = new String[keySet.size()];
		int i = 0;
		for (Iterator<String> it = keySet.iterator(); it.hasNext();) {
			keys[i] = it.next();
			i++;
		}
		charsetBox = new JComboBox<String>(keys);
		southCPanel.add(charsetBox);
		charsetBox.setSelectedItem(Charset.defaultCharset().name());

		b64EncodeBtn = new JButton("Base64编码");
		southCPanel.add(b64EncodeBtn);
		b64EncodeBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				doB64Encode();
			}
		});
		
		b64DecodeBtn = new JButton("Base64解码");
		southCPanel.add(b64DecodeBtn);
		b64DecodeBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				doB64Decode();
			}
		});
		
		encodeBtn = new JButton("二维码编码");
		southCPanel.add(encodeBtn);
		encodeBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				doEncode();
			}
		});
		southCPanel.add(new JLabel("宽"));
		imgWidthText = new JTextField(String.valueOf(DEFAULT_ENCODE_IMAGE_WIDTH), 4);
		imgWidthText.setHorizontalAlignment(JTextField.CENTER);
		southCPanel.add(imgWidthText);
		southCPanel.add(new JLabel("高"));
		imgHeightText = new JTextField(String.valueOf(DEFAULT_ENCODE_IMAGE_HEIGHT), 4);
		imgHeightText.setHorizontalAlignment(JTextField.CENTER);
		southCPanel.add(imgHeightText);
		
		decodeBtn = new JButton("二维码解码");
		southCPanel.add(decodeBtn);
		decodeBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				doDecode();
			}
		});
		
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	}
	
	private void doSelectImg() {
		int returnVal = fc.showOpenDialog(owner);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File imgFile = fc.getSelectedFile();
			imgPathText.setText(imgFile.getPath());
			loadAndShowImage(false);
		}
	}
	
	private void doEncode() {
		String imgInfo = imgInfoText.getText();
		if (imgInfo == null || imgInfo.length() == 0) return;
		
		BufferedImage img = null;
		Charset charset = availableCharsets.get(charsetBox.getSelectedItem());
		int imgWidth = DEFAULT_ENCODE_IMAGE_WIDTH;
		int imgHeight = DEFAULT_ENCODE_IMAGE_HEIGHT;
		try {
			imgWidth = Integer.parseInt(imgWidthText.getText());
			imgHeight = Integer.parseInt(imgHeightText.getText());
		} catch (Exception e) {
			imgWidth = DEFAULT_ENCODE_IMAGE_WIDTH;
			imgHeight = DEFAULT_ENCODE_IMAGE_HEIGHT;
		}
		try {
			img = QRCode.getInstance().encode(imgInfo, charset, imgWidth, imgHeight);
		} catch (Exception e) {
			img = null;
			String errorMsg = "编码错误：" + e.getMessage();
			JOptionPane.showMessageDialog(owner, errorMsg, "编码错误", JOptionPane.ERROR_MESSAGE);
			LogUtil.log(this, errorMsg);
		}
		if (img == null) return;
		
		showImage(img);
		imgPathText.setText("");
		currImgPath = "";
	}
	
	private void loadAndShowImage(boolean sync) {
		if (isLoading) return;
		
		isLoading = true;
		if (sync) {
			doLoadAndShowImage();
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					doLoadAndShowImage();
				}
			});
		}
	}
	
	private void doLoadAndShowImage() {
		String newImgPath = imgPathText.getText();
		if (newImgPath == null || newImgPath.length() == 0) {
			isLoading = false;
			return;
		}
		if (newImgPath.equals(currImgPath)) {
			isLoading = false;
			return;
		}
		
		URL imgURL = null;
		File imgFile = null;
		try {
			imgURL = new URL(newImgPath);
		} catch (MalformedURLException e) {
			imgURL = null;
			imgFile = new File(newImgPath);
		}
		
		BufferedImage img = null;
		try {
			if (imgURL != null) {
				img = ImageIO.read(imgURL);
			} else {
				img = ImageIO.read(imgFile);
			}
		} catch (Exception e) {
			img = null;
			LogUtil.log(this, e.getMessage());
		}
		if (img == null) {
			isLoading = false;
			return;
		}
		
		showImage(img);
		currImgPath = newImgPath;
		isLoading = false;
	}
	
	private void showImage(BufferedImage img) {
		currImg = img;
		fitImage();
		imgIcon.setText("");
	}
	
	private void fitImage() {
		if (currImg == null) return;
		
		ImageIcon icon = new ImageIcon(currImg);
		int fitWidth = Math.max(10, imgIcon.getWidth() - 30);
		int fitHeight = Math.max(10, imgIcon.getHeight() - 40);
		if (icon.getIconWidth() > fitWidth || icon.getIconHeight() > fitHeight) {
			float scale = Math.min(fitWidth * 1.0f / icon.getIconWidth(), fitHeight * 1.0f / icon.getIconHeight());
			icon.setImage(ImageUtil.scaleImage(icon.getImage(), scale));
		}
		imgIcon.setIcon(icon);
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				imgIcon.repaint();
			}
		});
	}
	
	private void doDecode() {
		if (isLoading) return;
		
		loadAndShowImage(true);
		
		if (currImg == null) return;
		
		Charset charset = availableCharsets.get(charsetBox.getSelectedItem());
		try {
			String decodedString = QRCode.getInstance().decode(currImg, charset);
			imgInfoText.setText(decodedString);
		} catch (Exception e) {
			String errorMsg = "解析错误：" + e.getMessage();
			imgInfoText.setText(errorMsg);
			LogUtil.log(this, errorMsg);
		}
	}

	private void doB64Encode() {
		String imgInfo = imgInfoText.getText();
		if (imgInfo == null || imgInfo.length() == 0) return;
		
		Charset charset = availableCharsets.get(charsetBox.getSelectedItem());
		String decodedStr;
		String encodedStr;
		
		decodedStr = imgInfoText.getSelectedText();
		if (decodedStr != null) {
			int startIdx = imgInfoText.getSelectionStart();
			encodedStr = new String(Base64.encodeBase64(decodedStr.getBytes(charset)), charset);
			imgInfoText.replaceSelection(encodedStr);
			imgInfoText.setSelectionStart(startIdx);
			imgInfoText.setSelectionEnd(startIdx + encodedStr.length());
			imgInfoText.requestFocus();
		} else {
			boolean isShadowsocks = imgInfo.startsWith("ss://");
			decodedStr = isShadowsocks ? imgInfo.substring(5) : imgInfo;
			encodedStr = new String(Base64.encodeBase64(decodedStr.getBytes(charset)), charset);
			if (isShadowsocks) {
				imgInfoText.setText("ss://" + encodedStr);
			} else {
				imgInfoText.setText(encodedStr);
			}
		}
	}

	private void doB64Decode() {
		String imgInfo = imgInfoText.getText();
		if (imgInfo == null || imgInfo.length() == 0) return;
		
		Charset charset = availableCharsets.get(charsetBox.getSelectedItem());
		String encodedStr;
		String decodedStr;
		
		encodedStr = imgInfoText.getSelectedText();
		if (encodedStr != null) {
			int startIdx = imgInfoText.getSelectionStart();
			decodedStr = new String(Base64.decodeBase64(encodedStr), charset);
			imgInfoText.replaceSelection(decodedStr);
			imgInfoText.setSelectionStart(startIdx);
			imgInfoText.setSelectionEnd(startIdx + decodedStr.length());
			imgInfoText.requestFocus();
		} else {
			boolean isShadowsocks = imgInfo.startsWith("ss://");
			encodedStr = isShadowsocks ? imgInfo.substring(5) : imgInfo;
			decodedStr = new String(Base64.decodeBase64(encodedStr), charset);
			if (isShadowsocks) {
				imgInfoText.setText("ss://" + decodedStr);
			} else {
				imgInfoText.setText(decodedStr);
			}
		}
	}
	
	private boolean canPasteImage() {
		return Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(DataFlavor.imageFlavor);
	}
	
	private void pasteImage() {
		Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
		if (t != null && t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
			try {
				BufferedImage image = (BufferedImage) t.getTransferData(DataFlavor.imageFlavor);
				showImage(image);
				imgPathText.setText("");
				currImgPath = "";
			} catch (Exception e) {
				LogUtil.log(this, e.getMessage());
			}
		}
	}
	
	private void copyImage() {
		MyImageTransferable imgTransferable = new MyImageTransferable(currImg);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgTransferable, null);
	}
	
	private void saveImage() {
		int returnVal = fc.showSaveDialog(owner);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File imgFile = fc.getSelectedFile();
			try {
				ImageIO.write(currImg, DEFAULT_SAVE_IMAGE_FORMAT, imgFile);
				imgPathText.setText(imgFile.getPath());
			} catch (IOException e) {
				String errorMsg = "保存错误：" + e.getMessage();
				JOptionPane.showMessageDialog(owner, errorMsg, "保存错误", JOptionPane.ERROR_MESSAGE);
				LogUtil.log(this, errorMsg);
			}
		}
	}
	
	class MyImageTransferable implements Transferable {

		private Image image;
		
		public MyImageTransferable(Image image) {
			this.image = image;
		}
		
		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { DataFlavor.imageFlavor };
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return DataFlavor.imageFlavor.equals(flavor);
		}

		@Override
		public Object getTransferData(DataFlavor flavor)
				throws UnsupportedFlavorException, IOException {
			
			if (!DataFlavor.imageFlavor.equals(flavor)) {
				throw new UnsupportedFlavorException(flavor);
			}
			return image;
		}
		
	}
	
}
