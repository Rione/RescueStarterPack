package rione.viewer.component;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	public AdvancedViewComponent(StandardWorldModel w) {
		super();
		world = w;

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

		controller = createController();
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

	// 一応残しておくけど、render自体もオーバーライド前提なのに、いらんやろ、普通。
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
		Map<EntityID, EntityExtension> extensionMap = createColorMap();
		for (RenderedObject next : result) {
			try {
				EntityID id = ((AbstractEntity) next.getObject()).getID();
				Shape shape = next.getShape();
				if (shape == null) continue;
				EntityExtension extension = extensionMap.get(id);

				g.setColor(extension.getColor());
				g.setStroke(extension.getStroke());
				if (extension.getFill()) {
					g.fill(shape);
				} else {
					g.draw(shape);
				}
			} catch (NullPointerException e) {
			} catch (ClassCastException e) {
			} catch (InternalError e) {
			}
		}

		try {
			// フォーカスされたEntityがAgentで、最後に呼び出されたメソッドが公開されていれば表示する
			DebugAgent agent = getFocusAgent();

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
				agent.customRender(g, t, width, height);
			}
		} catch (NullPointerException e) {
		}

		// ルーラー表示
		if (startX >= 0 && startY >= 0) {
			g.setColor(Color.ORANGE);
			g.drawLine(startX, startY, endX, endY);
			g.drawString(Double.toString(Math.hypot(t.screenToX(startX)
					- t.screenToX(endX), t.screenToY(startY)
					- t.screenToY(endY))), 16, 32);
		}

		// ExpandedBlockade確認用
		// for (RenderedObject next : result) {
		// try {
		// Blockade b = (Blockade) next.getObject();
		// if (b.isApexesDefined()) {
		// g.setColor(new Color(0, 0, 0, 128));
		// g.fill(rione.util.Geometry.expandApexes(b.getApexes(), 500, t));
		// g.setColor(Color.PINK);
		// g.draw(next.getShape());
		// }
		// } catch (IllegalAccessException e) {
		// e.printStackTrace();
		// } catch (ClassCastException e) {
		// }
		// }

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
	 * @return
	 * @throws NullPointerException
	 */
	protected DebugAgent getFocusAgent() throws NullPointerException {
		// フォーカスされているEntityを取得
		DebugAgent agent = AgentList.get(controller.getFocus());
		if (agent == null)
			throw new NullPointerException();
		return agent;
	}

	/**
	 * render用に装飾マップを生成
	 * 
	 * @return EntityIDと装飾(EntityExtension)の対応をあらわすマップ
	 */
	protected Map<EntityID, EntityExtension> createColorMap() {
		// 結果となる装飾マップ
		Map<EntityID, EntityExtension> result = new HashMap<EntityID, EntityExtension>();
		try {
			DebugAgent agent = getFocusAgent();
			// フォーカスがエージェントなら以下

			// 視界
			if (controller.visibleEntity() && world.getEntity(agent.getID()) instanceof Human) {
				Set<EntityID> visibleIDs = agent.getVisibleEntity();
				if (visibleIDs != null) {
					for (EntityID id : visibleIDs) {
						result.put(id, new EntityExtension(Color.PINK));
					}
				}
			}

			// Civilianの色を反映
			Color entityColor = controller.getCivilianColor();
			if (entityColor != null) {
				for (StandardEntity entity : world
						.getEntitiesOfType(StandardEntityURN.CIVILIAN)) {
					result
							.put(entity.getID(), new EntityExtension(
									entityColor));
				}
			}
			// ATの色を反映
			entityColor = controller.getAmbulanceTeamColor();
			if (entityColor != null) {
				for (StandardEntity entity : world
						.getEntitiesOfType(StandardEntityURN.AMBULANCE_TEAM)) {
					result
							.put(entity.getID(), new EntityExtension(
									entityColor));
				}
			}
			// FBの色を反映
			entityColor = controller.getFireBrigadeColor();
			if (entityColor != null) {
				for (StandardEntity entity : world
						.getEntitiesOfType(StandardEntityURN.FIRE_BRIGADE)) {
					result
							.put(entity.getID(), new EntityExtension(
									entityColor));
				}
			}
			// PFの色を反映
			entityColor = controller.getPoliceForceColor();
			if (entityColor != null) {
				for (StandardEntity entity : world
						.getEntitiesOfType(StandardEntityURN.POLICE_FORCE)) {
					result
							.put(entity.getID(), new EntityExtension(
									entityColor));
				}
			}

			// 指定色で上書き
			Map<EntityID, EntityExtension> overwriteExtension = createOverwriteExtension(
					controller.getCivilianColor(), StandardEntityURN.CIVILIAN);
			result.putAll(overwriteExtension);
			overwriteExtension = createOverwriteExtension(controller
					.getAmbulanceTeamColor(), StandardEntityURN.AMBULANCE_TEAM);
			result.putAll(overwriteExtension);
			overwriteExtension = createOverwriteExtension(controller
					.getFireBrigadeColor(), StandardEntityURN.FIRE_BRIGADE);
			result.putAll(overwriteExtension);
			overwriteExtension = createOverwriteExtension(controller
					.getPoliceForceColor(), StandardEntityURN.POLICE_FORCE);
			result.putAll(overwriteExtension);

			// 独自の効果を反映
			if (controller.costomExtention()) {
				try {
					result.putAll(agent.customExtension());
				} catch (NullPointerException e) {
					// nullが返ってきた場合
				}
			}
		} catch (NullPointerException e) {
			// エージェントでない場合はこっちに来る
		} finally {
			// フォーカス
			EntityID forcus = controller.getFocus();
			if (forcus != null) {
				result.put(forcus, new EntityExtension(Color.YELLOW,
						new BasicStroke(2.0f)));
			}
		}
		return result;
	}

	/**
	 * 上書き装飾マップを生成
	 * 
	 * @param c
	 * @param urn
	 * @return
	 */
	private Map<EntityID, EntityExtension> createOverwriteExtension(Color c,
			StandardEntityURN urn) {
		Map<EntityID, EntityExtension> result = new HashMap<EntityID, EntityExtension>();
		if (c != null) {
			EntityExtension ex = new EntityExtension(c, true);
			for (StandardEntity se : world.getEntitiesOfType(urn)) {
				result.put(se.getID(), ex);
			}
		}
		return result;
	}

	// /**
	// * Do whatever needs doing after the layers are painted.
	// */
	// protected void postpaint() {}

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

	/**
	 * コントローラを生成
	 * 
	 * @return
	 */
	private Controller createController() {
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
