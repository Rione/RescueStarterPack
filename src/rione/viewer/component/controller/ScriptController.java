package rione.viewer.component.controller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JFileChooser;

import rescuecore2.worldmodel.EntityID;
import rione.viewer.component.AdvancedViewComponent;

/*
 * JavaScript(*.js)の書き方
 * 	メソッドが毎サイクル呼ばれるのでそれを定義してやればよい
 * 	$HOME/resq.jsがあればそれを実行する
 * 	環境変数HOMEを設定すればWindowsでもおｋ
 * 	なければダイアログを出す
 * 	引数は全てサイクル数
 * 	定義しない場合は無視される
 * 	()内は値か戻り値の型
 * 
 * 	変数
 * 		debug
 * 			エラー表示をするか(true, false)
 * 	メソッド
 * 		focus(t)
 * 			フォーカスするID(整数)
 * 		visible(t)
 * 			視界内表示をするか(true, false)
 * 		plot(t)
 * 			worldmodelの座標表示
 * 		extension(t)
 * 			自由装飾を表示するか(true, false)
 * 		render(t)
 * 			自由描画を表示するか(true, false)
 * 		command(t)
 * 			コマンドを表示するか(true, false)
 * 		saveFolder(t)
 * 			保存先フォルダ(文字列)
 * 		saveImg(t)
 * 			画像を保存するか(true, false)
 * 		saveLog(t)
 * 			独自ログを出すか(true, false)
 * 		followFocus(t)
 * 			カメラ追従(true, false)
 * 		cvColor(t)
 * 			CVの色(16進)
 * 		atColor(t)
 * 			ATの色(16進)
 * 		fbColor(t)
 * 			FBの色(16進)
 * 		pfColor(t)
 * 			PFの色(16進)
 * 		update(t)
 * 			毎サイクル呼ばれる
 * 			文字列
 * 			"ファイルパス"
 * 				新しいスクリプトを起動
 * 			"newPanel"
 * 				PanelControllerを起動
 */

/**
 * スクリプトで操作するコントローラ
 * 
 * @author utisam
 * 
 */
public class ScriptController implements Controller {

	/** コントロール対象 */
	private final AdvancedViewComponent viewer;

	private boolean scriptDebug = false;
	private EntityID currentFocus = null;
	private boolean visibleEntity = false;
	private boolean customExtension = false;
	private boolean customRender = false;
	private boolean lastCommand = false;
	private boolean plotLocation = false;
	private String saveFolderName = null;
	private boolean saveImage = false;
	private boolean saveLog = false;
	private boolean followFocus = false;
	private Color cvColor;
	private Color atColor;
	private Color fbColor;
	private Color pfColor;
	private int frameWidth = 0, frameHigh = 0;

	/** スクリプト呼び出しのインタフェース */
	protected Invocable inv = null;
	
	private final static String funcFocus = "focus";
	private final static String funcVisible = "visible";
	private final static String funcExtension = "extension";
	private final static String funcRender = "render";
	private final static String funcCommand = "command";
	private final static String funcPlot = "plot";
	private final static String funcSaveFolder = "saveFolder";
	private final static String funcSaveImg = "saveImg";
	private final static String funcSaveLog = "saveLog";
	private final static String funcFollowFocus = "followFocus";
	private final static String funcCvColor = "cvColor";
	private final static String funcAtColor = "atColor";
	private final static String funcFbColor = "fbColor";
	private final static String funcPfColor = "pfColor";
	private final static String funcUpdate = "update";
	private static final String varDebugFlg = "debug";
	private static final String varFrameWidth = "width";
	private static final String varFrameHigh = "high";

	/**
	 * Controllerを引き継いでコンストラクト
	 * 
	 * @param c
	 */
	public ScriptController(Controller c) {
		this(c.getViewComponent(), null);
	}

	/**
	 * ControllerとScriptを引き継いでコンストラクト
	 * 
	 * @param c
	 * @param newScript
	 */
	public ScriptController(Controller c, File newScript) {
		this(c.getViewComponent(), newScript);
		// コントローラを引き継ぐ
		setFocus(c.getFocus());
		visibleEntity = c.visibleEntity();
		customExtension = c.costomExtention();
		customRender = c.customRender();
		lastCommand = c.lastCommand();
		plotLocation = c.plotLocation();
		saveFolderName = c.getSaveFolderName();
		saveImage = c.saveImage();
		saveLog = c.saveLog();
		followFocus = c.followFocus();
		cvColor = c.getCivilianColor();
		atColor = c.getAmbulanceTeamColor();
		fbColor = c.getFireBrigadeColor();
		pfColor = c.getPoliceForceColor();
	}

	/**
	 * 初回用コンストラクタ
	 */
	public ScriptController(AdvancedViewComponent v, File newScript) {
		viewer = v;

		// スクリプトエンジン生成
		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("JavaScript");

		// 評価
		try {
			engine.eval(new FileReader((newScript == null) ? createScriptFile() : newScript));
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (ScriptException e) {
			e.printStackTrace();
		}
		catch (NullPointerException e) {
			try {
				engine.eval("function update(t) {return 'newPanel';}");
			}
			catch (ScriptException e1) {
				e1.printStackTrace();
			}
		}

		// デバッグするかを設定
		try {
			scriptDebug = (Boolean) engine.get(varDebugFlg);
		}
		catch (ClassCastException e) {
			System.err.println("スクリプトのdebugが不正です");
			scriptDebug = false;
		}
		catch (NullPointerException e) {
			scriptDebug = false;
		}

		// フレームの大きさ
		try {
			frameWidth = ((Double) engine.get(varFrameWidth)).intValue();
		}
		catch (ClassCastException e) {
			System.err.println("スクリプトのwidthが不正です");
			frameWidth = 0;
		}
		catch (NullPointerException e) {
			frameWidth = 0;
		}
		try {
			frameHigh = ((Double) engine.get(varFrameHigh)).intValue();
		}
		catch (ClassCastException e) {
			System.err.println("スクリプトのhighが不正です");
			frameHigh = 0;
		}
		catch (NullPointerException e) {
			frameHigh = 0;
		}

		// アクセスのためのインタフェースだけを保持
		inv = (Invocable) engine;
	}

	/**
	 * スクリプトファイルを生成する<br>
	 * "${HOME}/resq.js"を参照する<br>
	 * Windowsでも環境変数設定すればおｋ
	 * 
	 * @return
	 */
	private static File createScriptFile() {
		// resq.jsを読む
		File defaultScript = null;
		try {
			String rcrsdir = System.getenv("RCRS_HOME");
			//TODO HOMEからRCRS_HOMEへ移行する
			if (rcrsdir == null) {
				rcrsdir = System.getenv("HOME");
			}
			
			if (rcrsdir != null) {
				defaultScript = new File(rcrsdir, "resq.js");
			}
		}
		catch (SecurityException e) {
			System.err.println("createScriptFile: permission denied");
			defaultScript = null;
		}
		// resq.jsがあればそれを返す
		if (defaultScript != null && defaultScript.exists()) {
			return defaultScript;
		}
		else {
			// 無ければファイル選択ダイアログを出す
			JFileChooser fc = new JFileChooser();
			fc.showDialog(new Frame("*.js"), "読み込み");
			return fc.getSelectedFile();
		}
	}

	@Override
	public Controller update(int time) {
		currentFocus = currentFocusResult(time);
		visibleEntity = visibleEntityResult(time);
		customExtension = customExtensionResult(time);
		customRender = customRenderResult(time);
		lastCommand = lastCommandResult(time);
		plotLocation = plotLocationResult(time);
		saveFolderName = saveFolderNameResult(time);
		saveImage = saveImageResult(time);
		saveLog = saveLogResult(time);
		followFocus = followFocusResult(time);
		cvColor = agentColorResult(time, funcCvColor);
		atColor = agentColorResult(time, funcAtColor);
		fbColor = agentColorResult(time, funcFbColor);
		pfColor = agentColorResult(time, funcPfColor);
		return updateResult(time);
	}

	private boolean plotLocationResult(int t) {
		Boolean result = false;
		try {
			result = (Boolean) inv.invokeFunction("plot", t);
		}
		catch (ScriptException e) {
			// スクリプト内エラー
			if (scriptDebug)
				e.printStackTrace();
		}
		catch (ClassCastException e) {
			// Boolean以外の値
			if (scriptDebug)
				e.printStackTrace();
		}
		catch (NoSuchMethodException e) {
			// メソッドが見つからない
			if (scriptDebug)
				System.out.println("no plot method");
		}
		return result;
	}

	/**
	 * スクリプトからFocusを取得
	 * 
	 * @param t
	 * @return
	 */
	private EntityID currentFocusResult(int t) {
		Integer result;
		try {
			// Integerに直接キャストするとClassCastException
			result = ((Double) inv.invokeFunction(funcFocus, t)).intValue();
			if (currentFocus == null || result != currentFocus.getValue()) {
				return new EntityID(result);
			}
		}
		catch (ScriptException e) {
			// スクリプト内エラー
			if (scriptDebug)
				e.printStackTrace();
		}
		catch (ClassCastException e) {
			// Integer以外の値
			if (scriptDebug)
				e.printStackTrace();
		}
		catch (NoSuchMethodException e) {
			// メソッドが見つからない
			if (scriptDebug)
				System.out.println("no focus method");
		}

		return currentFocus;
	}

	/**
	 * スクリプトからvisibleEntityを取得
	 * 
	 * @param t
	 * @return
	 */
	private boolean visibleEntityResult(int t) {
		Boolean result = false;
		try {
			result = (Boolean) inv.invokeFunction(funcVisible, t);
		}
		catch (ScriptException e) {
			// スクリプト内エラー
			if (scriptDebug)
				e.printStackTrace();
		}
		catch (ClassCastException e) {
			// Boolean以外の値
			if (scriptDebug)
				e.printStackTrace();
		}
		catch (NoSuchMethodException e) {
			// メソッドが見つからない
			if (scriptDebug)
				System.out.println("no visible method");
		}
		return result;
	}

	/**
	 * 
	 * @param t
	 * @return
	 */
	private boolean customExtensionResult(int t) {
		Boolean result = false;
		try {
			result = (Boolean) inv.invokeFunction(funcExtension, t);
		}
		catch (ScriptException e) {
			// スクリプト内エラー
			if (scriptDebug)
				e.printStackTrace();
		}
		catch (ClassCastException e) {
			// Boolean以外の値
			if (scriptDebug)
				e.printStackTrace();
		}
		catch (NoSuchMethodException e) {
			// メソッドが見つからない
			if (scriptDebug)
				System.out.println("no extension method");
		}
		return result;
	}

	/**
	 * 
	 * @param t
	 * @return
	 */
	private boolean customRenderResult(int t) {
		Boolean result = false;
		try {
			result = (Boolean) inv.invokeFunction(funcRender, t);
		}
		catch (ScriptException e) {
			// スクリプト内エラー
			if (scriptDebug)
				e.printStackTrace();
		}
		catch (ClassCastException e) {
			// Boolean以外の値
			if (scriptDebug)
				e.printStackTrace();
		}
		catch (NoSuchMethodException e) {
			// メソッドが見つからない
			if (scriptDebug)
				System.out.println("no render method");
		}
		return result;
	}

	/**
	 * 
	 * @param t
	 * @return
	 */
	private boolean lastCommandResult(int t) {
		Boolean result = false;
		try {
			result = (Boolean) inv.invokeFunction(funcCommand, t);
		}
		catch (ScriptException e) {
			// スクリプト内エラー
			if (scriptDebug)
				e.printStackTrace();
		}
		catch (ClassCastException e) {
			// Boolean以外の値
			if (scriptDebug)
				e.printStackTrace();
		}
		catch (NoSuchMethodException e) {
			// メソッドが見つからない
			if (scriptDebug)
				System.out.println("no command method");
		}
		return result;
	}

	/**
	 * 
	 * @param t
	 * @return
	 */
	private String saveFolderNameResult(int t) {
		Object result = null;
		try {
			result = inv.invokeFunction(funcSaveFolder, t);
		}
		catch (ScriptException e) {
			// スクリプト内エラー
			if (scriptDebug)
				e.printStackTrace();
		}
		catch (ClassCastException e) {
			// Boolean以外の値
			if (scriptDebug)
				e.printStackTrace();
		}
		catch (NoSuchMethodException e) {
			// メソッドが見つからない
			if (scriptDebug)
				System.out.println("no saveFolder method");
		}
		if (result == null) {
			return null;
		}
		return result.toString();
	}

	/**
	 * 
	 * @param t
	 * @return
	 */
	private boolean saveImageResult(int t) {
		Boolean result = false;
		try {
			result = (Boolean) inv.invokeFunction("saveImg", t);
		}
		catch (ScriptException e) {
			// スクリプト内エラー
			if (scriptDebug)
				e.printStackTrace();
		}
		catch (ClassCastException e) {
			// Boolean以外の値
			if (scriptDebug)
				e.printStackTrace();
		}
		catch (NoSuchMethodException e) {
			// メソッドが見つからない
			if (scriptDebug)
				System.out.println("no saveImg method");
		}
		return result;
	}

	/**
	 * 
	 * @param t
	 * @return
	 */
	private boolean saveLogResult(int t) {
		Boolean result = false;
		try {
			result = (Boolean) inv.invokeFunction(funcSaveLog, t);
		}
		catch (ScriptException e) {
			// スクリプト内エラー
			if (scriptDebug)
				e.printStackTrace();
		}
		catch (ClassCastException e) {
			// Boolean以外の値
			if (scriptDebug)
				e.printStackTrace();
		}
		catch (NoSuchMethodException e) {
			// メソッドが見つからない
			if (scriptDebug)
				System.out.println("no saveLog method");
		}
		return result;
	}

	public boolean followFocusResult(int t) {
		Boolean result = false;
		try {
			result = (Boolean) inv.invokeFunction(funcFollowFocus, t);
		}
		catch (ScriptException e) {
			// スクリプト内エラー
			if (scriptDebug)
				e.printStackTrace();
		}
		catch (ClassCastException e) {
			// Boolean以外の値
			if (scriptDebug)
				e.printStackTrace();
		}
		catch (NoSuchMethodException e) {
			// メソッドが見つからない
			if (scriptDebug)
				System.out.println("no followFocus method");
		}
		return result;
	}

	/**
	 * 
	 * @param t
	 * @param agent
	 * @return
	 */
	private Color agentColorResult(int t, String agentColorFunc) {
		Integer result = null;
		try {
			result = ((Double) inv.invokeFunction(agentColorFunc, t)).intValue();
		}
		catch (ScriptException e) {
			// スクリプト内エラー
			e.printStackTrace();
		}
		catch (ClassCastException e) {
			// Integer以外の値
			if (scriptDebug)
				e.printStackTrace();
		}
		catch (NoSuchMethodException e) {
			// メソッドが見つからない
			if (scriptDebug)
				System.out.println("no " + agentColorFunc + " method");
		}
		if (result == null)
			return null;
		return new Color(result);
	}

	/**
	 * 
	 * @param t
	 * @return
	 */
	private Controller updateResult(int t) {
		Object result = null;
		try {
			result = inv.invokeFunction(funcUpdate, t);
		}
		catch (ScriptException e) {
			// スクリプト内エラー
			if (scriptDebug)
				e.printStackTrace();
			return new PanelController(this);
		}
		catch (NoSuchMethodException e) {
			// メソッドが見つからない
			if (scriptDebug)
				System.out.println("no update method");
		}
		if (result == null) {
			return this;
		}
		if (result.equals("newPanel")) {
			// PanelController生成・データを引き継ぎ
			return new PanelController(this);
		}
		else if (result instanceof String) {
			File newScript = new File((String) result);
			if (newScript.exists()) {
				// ScriptController生成・データを引き継ぎ
				return new ScriptController(this, newScript);
			}
		}
		return this;
	}

	@Override
	public EntityID getFocus() {
		return currentFocus;
	}

	@Override
	public void setFocus(EntityID id) {
		currentFocus = id;
	}

	@Override
	public boolean visibleEntity() {
		return visibleEntity;
	}

	@Override
	public boolean costomExtention() {
		return customExtension;
	}

	@Override
	public boolean customRender() {
		return customRender;
	}

	@Override
	public boolean lastCommand() {
		return lastCommand;
	}

	@Override
	public String getSaveFolderName() {
		if (saveFolderName == null || saveFolderName.equals("")) {
			return null;
		}
		return saveFolderName;
	}

	@Override
	public boolean followFocus() {
		return followFocus;
	}

	@Override
	public Color getCivilianColor() {
		return cvColor;
	}

	@Override
	public Color getAmbulanceTeamColor() {
		return atColor;
	}

	@Override
	public Color getFireBrigadeColor() {
		return fbColor;
	}

	@Override
	public Color getPoliceForceColor() {
		return pfColor;
	}

	@Override
	public boolean saveImage() {
		return saveImage;
	}

	@Override
	public boolean saveLog() {
		return saveLog;
	}

	@Override
	public AdvancedViewComponent getViewComponent() {
		return viewer;
	}

	@Override
	public Dimension getFrameSize() {
		if (frameWidth == 0 || frameHigh == 0) {
			return null;
		}
		return new Dimension(frameWidth, frameHigh);
	}

	@Override
	public boolean plotLocation() {
		return plotLocation;
	}

	public static void createSettingScript(final Controller c) {
		final String[] funcNames = new String[] {
			funcFocus,
			funcVisible,
			funcExtension,
			funcRender,
			funcCommand,
			funcPlot,
			funcSaveFolder,
			funcSaveImg,
			funcSaveLog,
			funcFollowFocus,
			funcCvColor,
			funcAtColor,
			funcFbColor,
			funcPfColor,
			funcUpdate,
		};
		final EntityID focusedID = c.getFocus();
		final Object[] values = new Object[] {
			(focusedID != null) ? focusedID.getValue() : null,
			c.visibleEntity(),
			c.costomExtention(),
			c.customRender(),
			c.lastCommand(),
			c.plotLocation(),
			c.getSaveFolderName(),
			c.saveImage(),
			c.saveLog(),
			c.followFocus(),
			toHex(c.getCivilianColor()),
			toHex(c.getAmbulanceTeamColor()),
			toHex(c.getFireBrigadeColor()),
			toHex(c.getPoliceForceColor()),
			"'newPanel'",
		};
		final int n = funcNames.length;
		final File file = createScriptFile();
		System.out.println(file);
		if (file == null) return;
		try {
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			for (int i = 0; i < n; i++) {
				if (values[i] == null) continue;
				if (values[i] instanceof Boolean && !((Boolean) values[i])) continue;
				pw.printf("function %s(t) {return %s;}", funcNames[i], values[i]);
				pw.println();
				System.out.printf("function %s(t) {return %s;}", funcNames[i], values[i]);
				System.out.println();
			}
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static String toHex(Color c) {
		if (c == null) return null;
		return String.format("%06x", c.getRGB());
	}
}
