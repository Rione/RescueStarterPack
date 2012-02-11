package rione.viewer.component;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rescuecore2.misc.Pair;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.view.AnimatedWorldModelViewer;
import rescuecore2.view.RenderedObject;
import rescuecore2.view.ViewComponent;
import rescuecore2.view.ViewListener;
import rescuecore2.worldmodel.AbstractEntity;
import rescuecore2.worldmodel.EntityID;
import rione.agent.DebugAgent;
import rione.viewer.component.controller.Controller;
import rione.viewer.component.controller.PanelController;
import rione.viewer.component.controller.ScriptController;
import rione.viewer.component.extension.BorderExtension;
import rione.viewer.component.extension.EntityExtension;
import rione.viewer.component.extension.ExtensionMap;
import rione.viewer.component.extension.FillExtension;

/**
 * 世界を自由に描く程度の能力
 * 
 * @author utisam
 * 
 */
@SuppressWarnings("serial")
public class AdvancedViewComponent extends AnimatedWorldModelViewer {

	protected ScreenTransform transform = null;

	/* 起動時にaddTeamAgentから登録される */
	/**
	 * AgentのMap<br>
	 * エージェントからアクセスすると反則になるおそれがあるので，提出時には必ず確認すること．
	 */
	public static Map<EntityID, DebugAgent> AgentList = new HashMap<EntityID, DebugAgent>();

	/** コンストラクト時に貰った参照 */
	public StandardWorldModel world = null;

	/** コントローラ */
	public Controller controller = null;

	/** 始点 */
	private int startX = -1;
	private int startY = -1;

	/** 終点 */
	private int endX = -1;
	/** 終点 */
	private int endY = -1;

	public AdvancedViewComponent() {
		super();

		addViewListener(new ViewListener() {
			// ビューコンポーネントがクリックされたときに呼ばれる
			@Override
			public void objectsClicked(ViewComponent view,
					List<RenderedObject> objects) {
				for (RenderedObject next : objects) {
					Object obj = next.getObject();
					rione.viewer.AdvancedViewer.printObject(obj);
					if (obj instanceof AbstractEntity) {
						controller.setFocus(((AbstractEntity) obj).getID());
					}
				}
			}

			// マウスが重なったときかな？
			@Override
			public void objectsRollover(ViewComponent view,
					List<RenderedObject> objects) {
			}
		});

		this.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent e) {
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				if (e.isControlDown()) {
					if (startX < 0 && startY < 0) {
						startX = e.getX();
						startY = e.getY();
					}
					endX = e.getX();
					endY = e.getY();
					repaint();
				} else {
					startX = -1;
					startY = -1;
				}
			}
		});
	}

	/**
	 * ズームする
	 */
	public void zoomIn() {
		transform.zoomIn();
	}

	/**
	 * ズームをリセットする
	 */
	public void resetZoom() {
		transform.resetZoom();
	}

	/**
	 * ズームアウトする
	 */
	public void zoomOut() {
		transform.zoomOut();
	}

	/*
	 * Do whatever needs doing before the layers are painted.
	 */
	// protected void prepaint() {
	// }

	/*
	 * 描画時に呼び出される 1サイクルに1度とは限らない
	 */
	@Override
	protected Collection<RenderedObject> render(Graphics2D g,
			ScreenTransform t, int width, int height) {
		if (transform == null)
			transform = t;

		// 追従
		if (controller.followFocus()) {
			try {
				Human focus = (Human) world.getEntity(controller.getFocus());
				if (focus != null)
					transform.setCentrePoint(focus.getX(), focus.getY());
			} catch (NullPointerException e) {
			} catch (ClassCastException e) {
			}
		}

		// 親クラスの描画を行ない、そこまでに書かれたRenderObjectを取得
		Collection<RenderedObject> result = super.render(g, t, width, height);

		// 描画状態を一時保存
		Color tmpColor = g.getColor();
		Stroke tmpStroke = g.getStroke();

		// 装飾マップに従って描画
		ExtensionMap extensionMap = createColorMap();
		for (RenderedObject next : result) {
			Object obj = next.getObject();
			if (!(obj instanceof AbstractEntity)) continue;
			EntityID id = ((AbstractEntity) obj).getID();
			Shape shape = next.getShape();
			if (shape == null) continue;
			EntityExtension extension = extensionMap.get(id);
			if (extension == null) continue;
			Pair<Integer, Integer> location = world.getEntity(id).getLocation(world);
			extension.render(g, t, shape, location.first(), location.second());
		}
		// フォーカスされたEntityがAgentで、最後に呼び出されたメソッドが公開されていれば表示する
		DebugAgent agent = getFocusAgent();
		if (agent != null) {	
			// フォーカスのworldmodelの情報
			if (controller.plotLocation()) {
				StandardWorldModel aModel = agent.getWorld();
				g.setStroke(new BasicStroke(1.0f));
				g.setColor(Color.GRAY);
				drawHumanCoordinate(g, t, aModel,
						StandardEntityURN.AMBULANCE_TEAM);
				g.setColor(Color.ORANGE);
				drawHumanCoordinate(g, t, aModel,
						StandardEntityURN.FIRE_BRIGADE);
				g.setColor(Color.CYAN);
				drawHumanCoordinate(g, t, aModel,
						StandardEntityURN.POLICE_FORCE);
				g.setColor(new Color(128, 255, 128));
				drawHumanCoordinate(g, t, aModel, StandardEntityURN.CIVILIAN);
			}

			if (controller.lastCommand()) {
				g.setColor(new Color(255, 0, 0, 128));
				// System.err.println(agent.getLastCommandName());
				g.drawString(agent.getCommandsCall(), 16, 16);
			}
			if (controller.customRender()) {
				try {
					agent.customRender(g, t, width, height);
				}
				catch(ConcurrentModificationException e) {
					//同時アクセス
				}
			}
		}

		// ルーラー表示
		if (startX >= 0 && startY >= 0) {
			g.setColor(Color.ORANGE);
			g.drawLine(startX, startY, endX, endY);
			g.drawString(Double.toString(Math.hypot(t.screenToX(startX)
					- t.screenToX(endX), t.screenToY(startY)
					- t.screenToY(endY))), 16, 32);
		}

		// 復元
		g.setColor(tmpColor);
		g.setStroke(tmpStroke);
		return result;
	}

	private void drawHumanCoordinate(Graphics2D g, ScreenTransform t,
			StandardWorldModel model, StandardEntityURN urn) {
		for (StandardEntity se : model.getEntitiesOfType(urn)) {
			Human hm = (Human) se;
			if (hm.isPositionDefined()) {
				int sx = t.xToScreen(hm.getX());
				int sy = t.yToScreen(hm.getY());
				g.drawLine(sx - 10, sy - 10, sx + 10, sy + 10);
				g.drawLine(sx - 10, sy + 10, sx + 10, sy - 10);
			}
		}
	}

	/**
	 * フォーカスされているEntityがAgentならそれを取得
	 * 
	 * @return フォーカス中のエージェント
	 * @throws NullPointerException
	 */
	protected DebugAgent getFocusAgent() {
		// フォーカスされているEntityを取得
		DebugAgent agent = AgentList.get(controller.getFocus());
		return agent;
	}

	/**
	 * render用に装飾マップを生成
	 * 
	 * @return EntityIDと装飾(EntityExtension)の対応をあらわすマップ
	 */
	private ExtensionMap createColorMap() {
		// 結果となる装飾マップ
		ExtensionMap result = new ExtensionMap();
		DebugAgent agent = getFocusAgent();
		if (agent != null) {
			// 視界
			if (controller.visibleEntity() && world.getEntity(agent.getID()) instanceof Human) {
				result.putExtensionToAllID(agent.getVisibleEntities(), new BorderExtension(Color.PINK));
			}
	
			// 指定色で上書き
			Color c;
			if ((c = controller.getCivilianColor()) != null) {
				result.putExtensionToAll(world.getEntitiesOfType(StandardEntityURN.CIVILIAN),
						new FillExtension(c));
			}
			if ((c = controller.getAmbulanceTeamColor()) != null) {
				result.putExtensionToAll(world.getEntitiesOfType(StandardEntityURN.AMBULANCE_TEAM),
						new FillExtension(c));
			}
			if ((c = controller.getFireBrigadeColor()) != null) {
				result.putExtensionToAll(world.getEntitiesOfType(StandardEntityURN.FIRE_BRIGADE),
						new FillExtension(c));
			}
			if ((c = controller.getPoliceForceColor()) != null) {
				result.putExtensionToAll(world.getEntitiesOfType(StandardEntityURN.POLICE_FORCE),
						new FillExtension(c));
			}
			
			// 独自の効果を反映
			if (controller.costomExtention()) {
				try {
					Map<EntityID, EntityExtension> custom = agent.customExtension();
					if (custom != null) {
						result.putAll(custom);
					}
				}
				catch(ConcurrentModificationException e) {
					//同時アクセス
				}
				catch(Throwable e) {
					// customExtension内の例外対策
					e.printStackTrace();
				}
			}
		}
		// フォーカス
		EntityID forcus = controller.getFocus();
		if (forcus != null) {
			result.put(forcus, new BorderExtension(result.get(forcus), Color.YELLOW, 2.0f));
		}
		return result;
	}

	/**
	 * デバッグ用のエージェントを追加<br>
	 * 大会のチーム提出時には反則になる恐れがあるため絶対に呼び出さないこと<br>
	 * eclipseの呼び出し階層を開くから確認できる
	 * 
	 * @param agent
	 */
	public static void addTeamAgent(DebugAgent agent) {
		AgentList.put(agent.getID(), agent);
	}
	
	@Override
    public void view(Object...obj) {
		for (Object o : obj) {
			if (world != o && o instanceof StandardWorldModel) {
				world = (StandardWorldModel) o;
				if (controller == null) {
					controller = createController();
				}
			}
		}
		super.view(obj);
	}

	/**
	 * コントローラを生成
	 * 
	 * @return
	 */
	protected Controller createController() {
		if (rione.viewer.Viewer.SCRIPT_FLAG) {
			return new ScriptController(this, null);
		} else {
			return new PanelController(this);
		}
	}

	Object[] paintedObject = null;

	public void viewRepaint(Object... o) {
		paintedObject = o;
		view(o);
		repaint();
	}

	public void againRepaint() {
		if (paintedObject != null && paintedObject.length != 0)
			view(paintedObject);
		repaint();
	}
}
