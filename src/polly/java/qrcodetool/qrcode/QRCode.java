package polly.java.qrcodetool.qrcode;

import java.awt.image.BufferedImage;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QRCode {
	private static QRCode instance;
	public static QRCode getInstance() {
		if (instance == null) {
			instance = new QRCode();
		}
		return instance;
	}
	
	public static final String DEFAULT_DECODE_CHARSET = "ISO-8859-1";
	public static final String DEFAULT_ENCODE_CHARSET = "UTF-8";
	
	private MultiFormatReader barcodeReader;
	private HashMap<DecodeHintType, Object> readerHints;
	
	private MultiFormatWriter barcodeWriter;
	private HashMap<EncodeHintType, Object> writerHints;
	
	private QRCode() {
		barcodeReader = new MultiFormatReader();
		
		readerHints = new HashMap<DecodeHintType, Object>(2);
		ArrayList<BarcodeFormat> formats = new ArrayList<BarcodeFormat>(5);
		formats.add(BarcodeFormat.AZTEC);
		formats.add(BarcodeFormat.DATA_MATRIX);
		formats.add(BarcodeFormat.MAXICODE);
		formats.add(BarcodeFormat.PDF_417);
		formats.add(BarcodeFormat.QR_CODE);
		readerHints.put(DecodeHintType.POSSIBLE_FORMATS, formats);
		readerHints.put(DecodeHintType.CHARACTER_SET, "");
		
		barcodeWriter = new MultiFormatWriter();
		
		writerHints = new HashMap<EncodeHintType, Object>(2);
		writerHints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
		writerHints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
	}

	public BufferedImage encode(String data, Charset charset, int width, int height) throws QRCodeException {
		String charsetName = charset != null ? charset.name() : DEFAULT_ENCODE_CHARSET;
		writerHints.put(EncodeHintType.CHARACTER_SET, charsetName);
		BitMatrix bitMatrix = null;
		try {
			bitMatrix = barcodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height, writerHints);
		} catch (WriterException e) {
			String errorMsg = e.getMessage();
			if (errorMsg == null) errorMsg = "错误详情未知。";
			throw new QRCodeException(errorMsg);
		}
		BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);;
		return image;
	}
	
	public String decode(BufferedImage image, Charset charset) throws QRCodeException {
		LuminanceSource source = new BufferedImageLuminanceSource(image);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		String charsetName = charset != null ? charset.name() : DEFAULT_DECODE_CHARSET;
		readerHints.put(DecodeHintType.CHARACTER_SET, charsetName);
		barcodeReader.reset();
		Result result = null;
		try {
			result = barcodeReader.decode(bitmap, readerHints);
		} catch (NotFoundException e) {
			String errorMsg = e.getMessage();
			if (errorMsg == null) errorMsg = "错误详情未知。";
			throw new QRCodeException(errorMsg);
		}
		String data = result.getText();
		return data;
	}
}
