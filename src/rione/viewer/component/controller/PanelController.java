package rione.viewer.component.controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import rescuecore2.misc.EntityTools;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;
import rione.viewer.Viewer;
import rione.viewer.component.AdvancedViewComponent;

/**
 * GUIで操作するコントローラ
 *
 * @author utisam
 *
 */
@SuppressWarnings("serial")
public class PanelController extends JFrame implements Controller {

	protected static final int framewidth = 480;
	protected static final int width = 480 - 16;
	
	/** コントロール対象 */
	private final AdvancedViewComponent viewer;

	private static EntityID currentFocus = null;
	private static FocusSelectBox focusSelectBox = null;
	private static FocusURNSelectBox focusURNSelectBox = null;
	private static JPanel toplevelPanel = null;
	//private static JTextArea focusTextArea = null;
	private static JCheckBox visibleEntityCheckBox = null;
	private static JCheckBox customExtensionCheckBox = null;
	private static JCheckBox customRenderCheckBox = null;
	private static JCheckBox lastCommandCheckBox = null;
	private static JCheckBox plotLocationCheckBox = null;
	private static FolderPathBox folderNamePathBox = null;
	private static JCheckBox saveImageCheckBox = null;
	private static JCheckBox saveLogCheckBox = null;
	private static JCheckBox followFocusCheckBox = null;
	private static JCheckBox killAgentsCheckBox = null;
	private static ColorBox civilianColorBox = null;
	private static ColorBox atColorBox = null;
	private static ColorBox fbColorBox = null;
	private static ColorBox pfColorBox = null;

	/**
	 * Controllerを引き継いでコンストラクト
	 * @param c
	 */
	public PanelController(Controller c) {
		this(c.getViewComponent());
		setFocus(c.getFocus());
		//TODO コントロール追加時に要変更
		visibleEntityCheckBox.setSelected(c.visibleEntity());
		customExtensionCheckBox.setSelected(c.costomExtention());
		customRenderCheckBox.setSelected(c.customRender());
		lastCommandCheckBox.setSelected(c.lastCommand());
		plotLocationCheckBox.setSelected(c.plotLocation());
		String folder = c.getSaveFolderName();
		if (folder != null && !folder.equals("")) {
			folderNamePathBox.setText(folder);
		}
		saveImageCheckBox.setSelected(c.saveImage());
		saveLogCheckBox.setSelected(c.saveLog());
		followFocusCheckBox.setSelected(c.followFocus());
		killAgentsCheckBox.setSelected(c.killAgents());
		civilianColorBox.setColorValue(c.getCivilianColor());
		atColorBox.setColorValue(c.getAmbulanceTeamColor());
		fbColorBox.setColorValue(c.getFireBrigadeColor());
		pfColorBox.setColorValue(c.getPoliceForceColor());
	}

	/**
	 * 初期用コンストラクタ
	 * @param v
	 */
	public PanelController(AdvancedViewComponent v) {
		super();
		
		viewer = v;
		if (v == null) {
			System.err.println("PanelController is Test Mode");
		}
		
		Dimension minSize = new Dimension(framewidth, 640);
		Dimension maxSize = new Dimension(framewidth, 2048);
		setSize(minSize);
		setMinimumSize(minSize);
		setMaximumSize(maxSize);

		// 最上位パネル
		toplevelPanel = new JPanel();
		toplevelPanel.setSize(getSize());

		//ここに全部追加してからボックスを一気に並べる
		ArrayList<JComponent> boxList = new ArrayList<JComponent>() {
			@Override
			public boolean add(JComponent c) {
				// 同じキーリスナーを登録
				for (KeyListener l : viewer.getKeyListeners())
					c.addKeyListener(l);
				if (c instanceof Box) {
					//boxはそのまま
					return super.add(c);
				}
				else {
					//それ以外はボックスにする
					Box box = Box.createHorizontalBox();
					box.setMaximumSize(c.getSize());
					box.setSize(c.getSize());
					box.add(c);
					// 同じキーリスナーを登録
					return super.add(box);
				}
			}
		};
		
		/*
		 * ユーザインタフェースポリシー
		 * 	機能を追加しても開発メンバーが混乱しないために上から順に以下のように並べる．
		 * 
		 * 	フォーカス
		 * 		一番重要でよくいじるので
		 * 	自由描画系
		 * 		固定化された機能で満足したら限界が定まってしまうため
		 * 	主要固定機能
		 * 		「メソッド表示」とかはRescueではIDEのデバッガが使いにくいので役立てて欲しい
		 * 	ネタ機能
		 * 		ビューアいじる人も遊んだっていいじゃないか
		 * 		いつか役に立つかもしれないし
		 * 		追従とか誰得
		 */
		
		// コントローラの設定を保存する
		JButton saveButton = new JButton("SaveController");
		saveButton.setSize(new Dimension(width / 3, 24));
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ScriptController.createSettingScript(PanelController.this);	
			}
		});
		boxList.add(saveButton);
		
		// フォーカスするEntity
		focusSelectBox = new FocusSelectBox();
		boxList.add(focusSelectBox);
		
		// フォーカスするURN
		focusURNSelectBox = new FocusURNSelectBox();
		boxList.add(focusURNSelectBox);

		// 自由表示させるか
		customExtensionCheckBox = new JCheckBox("CustomExtension", false);
		customExtensionCheckBox.setSize(width, 40);
		boxList.add(customExtensionCheckBox);
		
		// 自由描画させるか
		customRenderCheckBox = new JCheckBox("CustomRender", false);
		customRenderCheckBox.setSize(width, 40);
		boxList.add(customRenderCheckBox);
		
		// メソッド表示
		lastCommandCheckBox = new JCheckBox("ShowLastCommand", false);
		lastCommandCheckBox.setSize(width, 40);
		boxList.add(lastCommandCheckBox);
		
		// 視界範囲のEntityを表示させるかのチェックボックス
		visibleEntityCheckBox = new JCheckBox("VisibleEntity", false);
		visibleEntityCheckBox.setSize(width, 40);
		boxList.add(visibleEntityCheckBox);
		
		// worldmodelの座標をプロットする
		plotLocationCheckBox = new JCheckBox("PlotLocation", false);
		plotLocationCheckBox.setSize(width, 40);
		boxList.add(plotLocationCheckBox);

		// 保存パス
		folderNamePathBox = new FolderPathBox();
		boxList.add(folderNamePathBox);
		
		// 画像保存
		saveImageCheckBox = new JCheckBox("SaveImageToPath", false);
		saveImageCheckBox.setSize(width, 40);
		boxList.add(saveImageCheckBox);
		
		// ログ保存
		saveLogCheckBox = new JCheckBox("SaveLogToPath", false);
		saveLogCheckBox.setSize(width, 40);
		boxList.add(saveLogCheckBox);
		
		// 追従させるか
		followFocusCheckBox = new JCheckBox("FollowFocus", false);
		followFocusCheckBox.setSize(width, 40);
		boxList.add(followFocusCheckBox);
		
		// シミュレーション終了時に終了させるか
		killAgentsCheckBox = new JCheckBox("KillAgents", false);
		killAgentsCheckBox.setSize(width, 40);
		boxList.add(killAgentsCheckBox);

		// 市民色
		civilianColorBox = new ColorBox("Civilian");
		boxList.add(civilianColorBox);

		// AT色
		atColorBox = new ColorBox("AmbulanceTeam");
		boxList.add(atColorBox);

		// FB色
		fbColorBox = new ColorBox("FireBrigade");
		boxList.add(fbColorBox);

		// PF色
		pfColorBox = new ColorBox("PoliceForce");
		boxList.add(pfColorBox);
		

		// 配置
		toplevelPanel.setLayout(new BoxLayout(toplevelPanel, BoxLayout.PAGE_AXIS));
		for (JComponent c : boxList) {
			toplevelPanel.add(c);
		}

		// スクロールペインにパネルを追加
		JScrollPane scrollPane = new JScrollPane(toplevelPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		// スクロールペインをFrameに追加
		add(scrollPane, BorderLayout.CENTER);
		//add(scrollPane);
		
		setVisible(true);
	}

	class ColorBox extends Box implements ChangeListener {
		private JCheckBox useDefaultColor = null;
		private JColorChooser customColorChooser = null;
		public ColorBox(String name) {
			super(BoxLayout.Y_AXIS);
			setMaximumSize(new Dimension(width, 40));
			setSize(width, 40);

			ArrayList<JComponent> boxList = new ArrayList<JComponent>() {
				@Override
				public boolean add(JComponent c) {
					//ボックスにする
					Box box = Box.createHorizontalBox();
					box.setMaximumSize(c.getSize());
					box.setSize(c.getSize());
					box.add(c);
					return super.add(box);
				}
			};

			// デフォルトを使うか
			useDefaultColor = new JCheckBox("UseDefault" + name  + "Color", true);
			useDefaultColor.addChangeListener(this);
			useDefaultColor.setMaximumSize(new Dimension(width, 400));
			useDefaultColor.setSize(width, 40);
			boxList.add(useDefaultColor);
			// 色選択
			customColorChooser = new JColorChooser();
			customColorChooser.setSize(customColorChooser.getMinimumSize());
			customColorChooser.setMaximumSize(customColorChooser.getMinimumSize());
			customColorChooser.setVisible(false);
			customColorChooser.remove(1);
			boxList.add(customColorChooser);

			for (JComponent c : boxList) {
				add(c);
			}
		}
		/** デフォルト色を使うか */
		public boolean useDefault() {
			return useDefaultColor.isSelected();
		}
		/** 設定された色 */
		public Color getColorValue() {
			return customColorChooser.getColor();
		}
		public void setColorValue(Color c) {
			if (c == null) {
				useDefaultColor.setSelected(true);
			}
			else {
				useDefaultColor.setSelected(false);
				customColorChooser.setColor(c);
			}
		}
		@Override
		public void stateChanged(ChangeEvent e) {
			boolean value = useDefaultColor.isSelected();
			customColorChooser.setVisible(!value);
			if (value) {
				setMaximumSize(new Dimension(width, 40));
				setSize(width, 40);
			}
			else {
				setMaximumSize(new Dimension(width, 400));
				setSize(width, 400);
			}
		}
	}
	
	class FolderPathBox extends Box {
		private JTextField pathTextBox = null;
		public FolderPathBox() {
			super(BoxLayout.LINE_AXIS);
			setMaximumSize(new Dimension(width, 32));
			setSize(width, 40);
			
			ArrayList<JComponent> boxList = new ArrayList<JComponent>() {
				@Override
				public boolean add(JComponent c) {
					//ボックスにする
					Box box = Box.createHorizontalBox();
					box.setMaximumSize(c.getSize());
					box.setSize(c.getSize());
					box.add(c);
					return super.add(box);
				}
			};
			
			final JLabel nameLabel = new JLabel("FolderPath");
			nameLabel.setSize(width / 4, 40);
			nameLabel.setMaximumSize(new Dimension(width * 1 / 4, 24));
			boxList.add(nameLabel);
			
			// カレントディレクトリをデフォルトとして設定する
			pathTextBox = new JTextField(System.getProperty("user.dir"));
			pathTextBox.setMaximumSize(new Dimension(width * 4 / 4, 40));
			pathTextBox.setSize(width * 5 / 8, 24);
			boxList.add(pathTextBox);
			
			for (JComponent c : boxList) {
				add(c);
			}
		}
		public void setText(String saveFolderName) {
			pathTextBox.setText(saveFolderName);		
		}
		public String getText() {
			return pathTextBox.getText();
		}
	}
	
	class FocusSelectBox extends Box implements ItemListener {
		private JComboBox entitiesComboBox = null;
		private Map<String, EntityID> strToID = null;
		private Map<EntityID, Integer> idToIndex = null;
		public FocusSelectBox() {
			super(BoxLayout.LINE_AXIS);
			setMaximumSize(new Dimension(width, 40));
			setSize(width, 40);
			
			ArrayList<JComponent> boxList = new ArrayList<JComponent>() {
				@Override
				public boolean add(JComponent c) {
					//ボックスにする
					Box box = Box.createHorizontalBox();
					box.setMaximumSize(c.getSize());
					box.setSize(c.getSize());
					box.add(c);
					return super.add(box);
				}
			};
			
			final JLabel nameLabel = new JLabel("FocusEntity");
			nameLabel.setSize(width / 4, 40);
			nameLabel.setMaximumSize(new Dimension(width * 2 / 8, 24));
			boxList.add(nameLabel);
			
			entitiesComboBox = new JComboBox();
			try {
				resetEntitiesComboBox(null);
			}
			catch (NullPointerException e) {}
			entitiesComboBox.setSize(width * 3 / 4, 24);
			entitiesComboBox.addItemListener(this);
			boxList.add(entitiesComboBox);
			
			for (JComponent c : boxList) {
				add(c);
			}
		}
		/** entitiesComboBoxを再生成する */
		private void resetEntitiesComboBox(Collection<StandardEntity> c) {
			entitiesComboBox.removeAllItems();
			if (c == null) {
				c = viewer.world.getEntitiesOfType(Viewer.INDEX_CLASS);
			}
			strToID = new HashMap<String, EntityID>(c.size());
			idToIndex = new HashMap<EntityID, Integer>(c.size());
			List<StandardEntity> entitiesList = new ArrayList<StandardEntity>(c);
			Collections.sort(entitiesList, new EntityTools.IDComparator());
			entitiesComboBox.addItem("Null");
			int i = 1;
			for (StandardEntity se : entitiesList) {
				String name = se.getClass().getSimpleName() + " (" + se.getID().getValue() + ")";
				strToID.put(name, se.getID());
				idToIndex.put(se.getID(), i);
				entitiesComboBox.addItem(name);
				i++;
			}
		}
		/** 選択されたIDを得る */
		public EntityID getSelectedItem() {
			return strToID.get(entitiesComboBox.getSelectedItem());
		}
		@Override
		public void itemStateChanged(ItemEvent e) {
			setFocus(getSelectedItem());
		}
		public void setSelectedItem(EntityID id) {
			// 無限ループになるので一旦外す
			entitiesComboBox.removeItemListener(this);
			try {
				if (id == null) {
					entitiesComboBox.setSelectedIndex(0);
				}
				else if (idToIndex != null) {
					Integer i = idToIndex.get(id);
					if (i == null) i = 0;
					entitiesComboBox.setSelectedIndex(i);
				}
			} 
			catch (IllegalArgumentException e){
			}
			entitiesComboBox.addItemListener(this);
		}
	}
	
	class FocusURNSelectBox extends Box implements ItemListener {
		private JComboBox urnComboBox = null;
		private Map<String, StandardEntityURN> strToURN = null;
		public FocusURNSelectBox() {
			super(BoxLayout.LINE_AXIS);
			setMaximumSize(new Dimension(width, 40));
			setSize(width, 40);
			
			ArrayList<JComponent> boxList = new ArrayList<JComponent>() {
				@Override
				public boolean add(JComponent c) {
					//ボックスにする
					Box box = Box.createHorizontalBox();
					box.setMaximumSize(c.getSize());
					box.setSize(c.getSize());
					box.add(c);
					return super.add(box);
				}
			};
			
			final JLabel nameLabel = new JLabel("FocusEntityURN");
			nameLabel.setSize(width / 4, 40);
			nameLabel.setMaximumSize(new Dimension(width * 2 / 8, 24));
			boxList.add(nameLabel);
			
			Vector<String> items = new Vector<String>();
			strToURN = new HashMap<String, StandardEntityURN>();
			items.add("Null");
			for (StandardEntityURN urn : Viewer.INDEX_CLASS) {
				strToURN.put(urn.name(), urn);
				items.add(urn.name());
			}
			Collections.sort(items);
			urnComboBox = new JComboBox(items);
			urnComboBox.setSize(width * 3 / 4, 24);
			urnComboBox.setMaximumRowCount(Viewer.INDEX_CLASS.length);
			urnComboBox.addItemListener(this);
			boxList.add(urnComboBox);
			urnComboBox.setSelectedItem("Null");
			
			for (JComponent c : boxList) {
				add(c);
			}
		}
		/** 選択されたIDを得る */
		public StandardEntityURN getSelectedItem() {
			return strToURN.get(urnComboBox.getSelectedItem());
		}
		@Override
		public void itemStateChanged(ItemEvent e) {
			try {
				StandardEntityURN urn = getSelectedItem();
				Collection<StandardEntity> entities = viewer.world.getEntitiesOfType(urn);
				focusSelectBox.resetEntitiesComboBox(entities);
			}
			catch (NullPointerException ex) {
				focusSelectBox.resetEntitiesComboBox(null);
			}
		}
	}
	
	@Override
	public Controller update(final int time) {
		return this;
	}

	@Override
	public EntityID getFocus() {
		return currentFocus;
	}

	@Override
	public void setFocus(EntityID id) {
		// 選択しているURNのEntity以外は選択しない
		try {
			StandardEntity se = viewer.world.getEntity(id);
			if (viewer.world.getEntitiesOfType(focusURNSelectBox.getSelectedItem()).contains(se)) {
				currentFocus = id;
			}
		}
		catch (NullPointerException e) {
			currentFocus = id;
		}
		// textAreaの更新
//		try {
//			focusTextArea.setText(AdvancedViewer.createDetailString(getFocus()));
//		} catch (NullPointerException e) {
//			focusTextArea.setText("Focus is NULL");
//		}
		// IDBoxの更新
		focusSelectBox.setSelectedItem(id);
		viewer.repaint();
	}

	@Override
	public boolean visibleEntity() {
		return visibleEntityCheckBox.isSelected();
	}

	@Override
	public boolean costomExtention() {
		return customExtensionCheckBox.isSelected();
	}
	
	@Override
	public boolean customRender() {
		return customRenderCheckBox.isSelected();
	}
	
	@Override
	public String getSaveFolderName() {
		return folderNamePathBox.getText();
	}
	
	@Override
	public boolean saveImage() {
		return saveImageCheckBox.isSelected();
	}

	@Override
	public boolean saveLog() {
		return saveLogCheckBox.isSelected();
	}
	
	@Override
	public boolean followFocus() {
		return followFocusCheckBox.isSelected();
	}

	@Override
	public Color getCivilianColor() {
		if (civilianColorBox.useDefault()) {
			return null;
		}
		return civilianColorBox.getColorValue();
	}

	@Override
	public Color getAmbulanceTeamColor() {
		if (atColorBox.useDefault()) {
			return null;
		}
		return atColorBox.getColorValue();
	}

	@Override
	public Color getFireBrigadeColor() {
		if (fbColorBox.useDefault()) {
			return null;
		}
		return fbColorBox.getColorValue();
	}

	@Override
	public Color getPoliceForceColor() {
		if (pfColorBox.useDefault()) {
			return null;
		}
		return pfColorBox.getColorValue();
	}

	@Override
	public boolean lastCommand() {
		return lastCommandCheckBox.isSelected();
	}
	
	@Override
	public boolean plotLocation() {
	    return plotLocationCheckBox.isSelected();
	}

	@Override
	public AdvancedViewComponent getViewComponent() {
		return viewer;
	}

	@Override
	public Dimension getFrameSize() {
		return null;
	}

	@Override
	public boolean killAgents() {
		return killAgentsCheckBox.isSelected();
	}
}
