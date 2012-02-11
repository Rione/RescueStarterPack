package rione.viewer;


import static rescuecore2.misc.java.JavaTools.instantiate;

import rescuecore2.messages.control.KVTimestep;
import rescuecore2.score.CompositeScoreFunction;
import rescuecore2.score.ScoreFunction;
import rescuecore2.Constants;
import rescuecore2.Timestep;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import java.util.ArrayList;
import java.util.Set;
import java.text.NumberFormat;

import rescuecore2.standard.components.StandardViewer;
import rescuecore2.standard.entities.StandardEntityURN;
import rione.viewer.component.AdvancedViewComponent;

/**
 * サンプルビューア＋α程度の能力<br>
 * 泥臭い処理が多いので読むのには苦労すると思う<br>
 * 拡張するならAdvancedな方推奨<br>
 * フラグもここにまとめる<br>
 */
public abstract class Viewer extends StandardViewer {

	/**
	 * GUIを作動させるかどうかのフラグ<br>
	 * これを切った場合のテストは怖くてしてない<br>
	 */
	protected static final boolean GUI_FLAG = true;
	/**
	 * 最初にScriptで操作するかどうかのフラグ<br>
	 * デフォルトを置いたり取り消しを押したりすればそれで済むのだけれど<br>
	 */
	public static final boolean SCRIPT_FLAG = true;

	/** 標準のタイムステップ数 300[step] */
	private static final int DEFAULT_TIMESTEP = 300;
	/** デフォルトのフォントサイズ */
	private static final int DEFAULT_FONT_SIZE = 20;
	/** スコアの小数点以下の桁数 */
	private static final int PRECISION = 3;

	/** タイムステップのキー */
	private static final String TIMESTEP_KEY = "kernel.timesteps";
	/** フォントサイズのキー */
	private static final String FONT_SIZE_KEY = "viewer.font-size";
	private static final String MAXIMISE_KEY = "viewer.maximise";
	//private static final String TEAM_NAME_KEY = "viewer.team-name";

	/** スコア関数 */
	protected ScoreFunction[] scoreFunctions;
	
	/** タイムステップ数 */
	public static int timestep;

	/** 世界を表示するビューコンポーネント */
	protected AdvancedViewComponent viewer = null;
	/** 時間を表示するラベル */
	private JLabel timeLabel;
	/** トータルスコアを表示するラベル */
	private JLabel scoreLabel;
	/** チーム名 */
	private JLabel teamLabel;

	/** 数値->文字列のフォーマット(主にスコアで使う) */
	protected NumberFormat format;
	public static final StandardEntityURN[] INDEX_CLASS = new StandardEntityURN[] {
		StandardEntityURN.AMBULANCE_CENTRE,
		StandardEntityURN.AMBULANCE_TEAM,
		StandardEntityURN.BLOCKADE,
		StandardEntityURN.BUILDING,
		StandardEntityURN.CIVILIAN,
		StandardEntityURN.FIRE_BRIGADE,
		StandardEntityURN.FIRE_STATION,
		StandardEntityURN.POLICE_FORCE,
		StandardEntityURN.POLICE_OFFICE,
		StandardEntityURN.REFUGE,
		StandardEntityURN.ROAD,
		StandardEntityURN.WORLD
	};

	/**
	 * 接続～初期化
	 */
	@Override
	protected void postConnect() {
		super.postConnect();
		System.out.println(toString() + " was connected.");

		timestep = config.getIntValue(TIMESTEP_KEY, DEFAULT_TIMESTEP);
		model.indexClass(INDEX_CLASS);

		makeScoreFunctions();
		format = NumberFormat.getInstance();
		format.setMaximumFractionDigits(PRECISION);
		if (GUI_FLAG) {
			initGUI();
		}
		this.initialize();
		// Controllerと初期状態のログのため
		update(0);
	}

	/**
	 * GUI生成
	 */
	protected void initGUI() {
		int fontSize = config.getIntValue(FONT_SIZE_KEY, DEFAULT_FONT_SIZE);
		String teamName = "Ri-one";// config.getValue(TEAM_NAME_KEY, "");
		JFrame frame = new JFrame("Viewer " + getViewerID() + " ("
				+ model.getAllEntities().size() + " entities)");
		viewer = new AdvancedViewComponent();
		viewer.initialise(config);
		viewer.view(model);
		// CHECKSTYLE:OFF:MagicNumber
		//コントローラからサイズ指定
		Dimension d = viewer.controller.getFrameSize();
		if (d == null) {
			viewer.setPreferredSize(new Dimension(640, 640));
		}
		else {
			viewer.setPreferredSize(d);
		}
		timeLabel = new JLabel("Time: Not started", SwingConstants.CENTER);
		teamLabel = new JLabel(teamName, SwingConstants.CENTER);
		scoreLabel = new JLabel("Score: Unknown", SwingConstants.CENTER);
		timeLabel.setBackground(Color.WHITE);
		timeLabel.setOpaque(true);
		timeLabel.setFont(timeLabel.getFont().deriveFont(Font.PLAIN,
				fontSize));
		teamLabel.setBackground(Color.WHITE);
		teamLabel.setOpaque(true);
		teamLabel.setFont(timeLabel.getFont().deriveFont(Font.PLAIN,
				fontSize));
		scoreLabel.setBackground(Color.WHITE);
		scoreLabel.setOpaque(true);
		scoreLabel.setFont(timeLabel.getFont().deriveFont(Font.PLAIN,
				fontSize));
		frame.add(viewer, BorderLayout.CENTER);
		JPanel labels = new JPanel(new GridLayout(1, 3));
		labels.add(teamLabel);
		labels.add(timeLabel);
		labels.add(scoreLabel);
		frame.add(labels, BorderLayout.NORTH);
		frame.pack();
		if (config.getBooleanValue(MAXIMISE_KEY, false)) {
			frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		}
		frame.addKeyListener(new ViewerEventProcessor(viewer));
		frame.setVisible(true);
	}

	/**
	 * 初期化時に呼ばれる
	 */
	protected abstract void initialize();

	/**
	 * 毎サイクル呼ばれる
	 */
	@Override
	protected void handleTimestep(final KVTimestep t) {
		try {
			super.handleTimestep(t);
			if (GUI_FLAG) {
				//非同期で更新させる
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						timeLabel.setText("Time: " + t.getTime());
						scoreLabel.setText("Score: " + format.format(scoreFunctions[0].score(model, new Timestep(t.getTime()))));
						viewer.viewRepaint(model, t.getCommands());
					}
				});
			}
			update(t.getTime());
			
			viewer.repaint();
		}
		catch(Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * 毎サイクルに1回呼ばれる
	 * @param time
	 */
	protected abstract void update(final int time);

	@Override
	public String toString() {
		return "Ri-one viewer";
	}

	/**
	 * スコア計算関数を取得
	 *
	 * @return
	 */
	private void makeScoreFunctions() {
		String className = config.getValue(Constants.SCORE_FUNCTION_KEY);
		ScoreFunction sf = instantiate(className, ScoreFunction.class);
		sf.initialise(model, config);
		ArrayList<ScoreFunction> scoreFuncs = new ArrayList<ScoreFunction>();
		makeScoreFunctions(scoreFuncs, sf);
		scoreFunctions = scoreFuncs.toArray(new ScoreFunction[]{});
	}
	private void makeScoreFunctions(ArrayList<ScoreFunction> scoreFuncs, ScoreFunction sf) {
		scoreFuncs.add(sf);
		if (sf instanceof CompositeScoreFunction) {
            Set<ScoreFunction> children = ((CompositeScoreFunction)sf).getChildFunctions();
            for (ScoreFunction next : children) {
                makeScoreFunctions(scoreFuncs, next);
            }
        }
	}
}
