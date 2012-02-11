package rione.viewer;


import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import rescuecore2.Timestep;
import rescuecore2.score.ScoreFunction;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;
import rione.agent.DebugAgent;
import rione.viewer.component.AdvancedViewComponent;
import rione.viewer.component.controller.Controller;

/**
 * Ri-one特有の機能を追加する程度の能力<br>
 * Viewer本体<br>
 * 主にイベントとかAdvancedWorldModelとか<br>
 * ファイルとしていろいろ出力したりもする<br>
 * 要望のあった表示以外の機能はココを中心に作る<br>
 * ってしてたらこっちの方が泥臭くなった
 *
 * @author utisam
 *
 */
public class AdvancedViewer extends Viewer {

	protected StandardWorldModel world = null;
	private static StandardWorldModel  staticModel = null;
	
	/**
	 * ログ内のPython変数名<br>
	 * List&gt;Map&gt;Object, Map&gt;Object, Object&lt;&lt;&lt;みたいなかんじになってる
	 */
	public final static String var = "logDics";

	/*
	 * 初期化時に呼ばれる
	 */
	@Override
	protected void initialize() {
		world = model;
		staticModel = model;
	}

	/*
	 * 毎サイクル呼ばれる
	 */
	@Override
	protected void update(int time) {
		if (!GUI_FLAG) {
			// 時間とスコアをコンソールに描画
			System.out.println("Score " + time + ":");
			for (ScoreFunction f : scoreFunctions) {
				System.out.printf("\t%32s\t%16f\n", f.getName(),
						f.score(model, new Timestep(time)));
			}
		}
		
		if (viewer.controller != null) {
			// updateから新しいControllerが来たら切り替える
			Controller next = viewer.controller.update(time);
			if (next != viewer.controller) {
				viewer.controller = next;
			}
			
			// 画像を保存
			if (viewer.controller.saveImage()) {
				saveViewerImage(viewer.controller.getSaveFolderName(), time);
			}
			
			// 独自のログを保存
			if (viewer.controller.saveLog()) {
				saveViewerLog(viewer.controller.getSaveFolderName(), time);
			}
			
			// 終了
			if (viewer.controller.killAgents() && time >= timestep) {
				System.exit(0);
			}
		}
	}
	
	/**
	 * 連番画像を保存する
	 * @param folderPath
	 * @param time
	 */
	protected void saveViewerImage(String folderPath, int time) {
		if (folderPath == null) return;
		try {
			Dimension d = viewer.getSize();
			BufferedImage img = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
			viewer.paint(img.createGraphics());
			if (!ImageIO.write(img, "PNG", new File(folderPath, "img" + Integer.toString(time) + ".png"))) {
				throw new IOException("PNGフォーマットが対象外？");
			}
			img.flush();
		}
		catch(IOException e) {
			System.err.println("SaveViewerImageでIO例外");
			e.printStackTrace();
		}
		catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		catch(NullPointerException e) {
			System.err.println("SaveViewerImageでぬるぽ");
			e.printStackTrace();
			System.err.println("ガッ");
		}
	}
	
	/**
	 * ログを出力する<br>
	 * ログはPythonで吐出されるため，インタラクティブシェルから自由に参照できる．
	 * 1サイクルごとにlog(time).pyというファイル名で別ファイルとなる．
	 * @param folderPath
	 * @param time
	 */
	protected void saveViewerLog(String folderPath, int time) {
		if (folderPath == null) return;
		// 書き込みを行うストリーム
		// デストラクタでcloseする
		PrintStream stream = null;
		File logFile = new File(folderPath, "log" + time + ".py");
		if (!logFile.exists()) {
			// 初回の書き込み
			stream = getPrintStream(logFile, false);
			stream.println("#!/usr/bin/env python");
			stream.println("#-*- coding:utf-8 -*-");
			stream.println("time = " + time);
		}
		if (stream == null) return;
		
		// スコア
		// Pythonの辞書("Key:Value,")を追加していく
		stream.println("score = {");
		for (ScoreFunction f : scoreFunctions) {
			//Key:スコアの文字列, Value:スコアの実数
			stream.printf("\t'%s': %f,",
					f.getName(),
					f.score(model, new Timestep(time)));
			stream.println();
		}
		stream.println('}');
		
		//DebugAgent
		stream.println("agent = {");
		for (Entry<EntityID, DebugAgent> entry : AdvancedViewComponent.AgentList.entrySet()) {
			EntityID id = entry.getKey();
			DebugAgent agent = entry.getValue();
			stream.printf("\t%d: {", id.getValue());
			stream.println();
			stream.printf("\t\t'calls': '%s',", agent.getCommandsCall());
			stream.println();
			stream.println("\t},");
		}
		stream.println('}');
		
		// WorldModelはRescueのサーバによるログから復元できるのでそちらを利用しましょう．
//		// このサイクルのEntity
//		for (StandardEntity se : model.getAllEntities()) {
//			// Pythonの辞書文字列("Key:Value, ")を追加していく
//			StringBuffer dictionary = new StringBuffer();
//			
//			try {
//				// Propatyを使ってもいいけど，こっちの方が細かく出力を調整できる気がした
//				// ログとして不必要なものはコメントアウトしたりしている
//				/* コピペ用
//				String
//					dictionary.append("'':'" +  + "', ");
//				int,double
//					dictionary.append("'':" +  + ", ");
//				boolean
//					dictionary.append("'':" + (() ? "True" : "False") + ", ");
//				 */
//				//dictionary.append("'name':'" + se.toString() + "', ");
//				if (se instanceof Human) {
//					Human human = (Human) se;
//					if (human.isHPDefined()) {
//						dictionary.append("'hp':");
//						dictionary.append(human.getHP());
//						dictionary.append(", ");
//					}
//					if (human.isDamageDefined()) {
//						dictionary.append("'damage':");
//						dictionary.append(human.getDamage());
//						dictionary.append(", ");
//					}
//					//if (human.isStaminaDefined())
//					//	dictionary.append("'stamina':" + human.getStamina() + ", ");
//					if (human.isBuriednessDefined()) {
//						dictionary.append("'buriedness':");
//						dictionary.append(human.getBuriedness());
//						dictionary.append(", ");
//					}
//					//if (human.isDirectionDefined())
//					//	dictionary.append("'direction':" + human.getDirection() + ", ");
//					if (human.isPositionDefined()) {
//						dictionary.append("'position':");
//						dictionary.append(human.getPosition().getValue());
//						dictionary.append(", ");
//					}
//					//if (human.isPositionHistoryDefined())
//					if (human.isTravelDistanceDefined()) {
//						dictionary.append("'travelDistance':");
//						dictionary.append(human.getTravelDistance());
//						dictionary.append(", ");
//					}
//					//if (human.isXDefined()) {
//					//	dictionary.append("'x':");
//					//	dictionary.append(human.getX());
//					//	dictionary.append(", ");
//					//}
//					//if (human.isYDefined()) {
//					//	dictionary.append("'y':");
//					//	dictionary.append(human.getY());
//					//	dictionary.append(", ");
//					//}
//
//					if (human instanceof AmbulanceTeam) {
//						dictionary.append("'type':'at', ");
//					}
//					else if (human instanceof FireBrigade) {
//						FireBrigade fb = (FireBrigade) human;
//						dictionary.append("'type':'fb', ");
//						if (fb.isWaterDefined()) {
//							dictionary.append("'water':");
//							dictionary.append(fb.getWater());
//							dictionary.append(", ");
//						}
//					}
//					else if (human instanceof PoliceForce) {
//						dictionary.append("'type':'pf', ");
//					}
//					else if (human instanceof Civilian) {
//						dictionary.append("'type':'civilian', ");
//					}
//					
//					// EntityをAgentに変換
//					DebugAgent dbgAgent = AdvancedViewComponent.AgentList.get(human.getID());
//					if (dbgAgent != null) {
//						StringBuffer methodsList = new StringBuffer();
//						for (String methods : dbgAgent.getCommandsCall().split("->")) {
//							methodsList.append('\'');
//							methodsList.append(methods);
//							methodsList.append("', ");
//						}
//						if (!methodsList.toString().equals("")) {
//							dictionary.append("'methods':[");
//							dictionary.append(methodsList.toString());
//							dictionary.append("], ");
//						}
//						
////						if (dbgAgent instanceof AmbulanceTeamAgent) {
////							AmbulanceTeamAgent atAgent = (AmbulanceTeamAgent) dbgAgent;
////							dictionary.append("'board':[");
////							dictionary.append(((atAgent.someoneOnBoard()) ? "True" : "False"));
////							dictionary.append("], ");
////						}
//						//else if (dbgAgent instanceof FireBrigadeAgent) {
//						//}
//						//else if (dbgAgent instanceof PoliceForceAgent) {
//						//}
//					}
//
//				} else if (se instanceof Area) {
//					Area area = (Area) se;
////					if (area.isXDefined())
////						dictionary.append("'x':" + area.getX() + ", ");
////					if (area.isYDefined())
////						dictionary.append("'y':" + area.getY() + ", ");
////					if (area.isEdgesDefined()) {
////						StringBuffer edgesList = new StringBuffer();
////						for (Edge e : area.getEdges()) {
////							edgesList.append( + ", ");
////						}
////						dictionary.append("'edges':[" + edgesList.toString() + "], ");
////					}
//					//if (area.isBlockadesDefined()) {
//					//	StringBuffer blockadesList = new StringBuffer();
//					//	for (EntityID b : area.getBlockades()) {
//					//		blockadesList.append(b.getValue() + ", ");
//					//	}
//					//	if (!blockadesList.toString().equals(""))
//					//		dictionary.append("'blockades':[" + blockadesList.toString() + "], ");
//					//}
//					
//					if (area instanceof Building) {
//						Building building = (Building) se;
//						if (building.isBrokennessDefined()) {
//							dictionary.append("'brokenness':");
//							dictionary.append(building.getBrokenness());
//							dictionary.append(", ");
//						}
//						if (building.isIgnitionDefined()) {
//							dictionary.append("'ignition':");
//							dictionary.append(((building.getIgnition()) ? "True" : "False"));
//							dictionary.append(", ");
//						}
//						if (building.isOnFire()) {
//							dictionary.append("'onFire':");
//							dictionary.append(((building.isOnFire()) ? "True" : "False"));
//							dictionary.append(", ");
//						}
//						if (building.isTemperatureDefined()) {
//							dictionary.append("'temperarure':");
//							dictionary.append(building.getTemperature());
//							dictionary.append(", ");
//						}
//						
//						if (area instanceof Refuge) {
////							Refuge refuge = (Refuge) se;
//							dictionary.append("'type':'refuge', ");
//						}
//						else {
//							dictionary.append("'type':'building', ");							
//						}
//					}
//					else if (area instanceof Road) {
////						Road road = (Road) obj;
//						dictionary.append("'type':'road', ");
//					}
//				}
//				else if (se instanceof Blockade) {
//					Blockade blockade = (Blockade) se;
//					dictionary.append("'type':'blockade', ");
//					if (blockade.isRepairCostDefined()) {
//						dictionary.append("'repairCost':");
//						dictionary.append(blockade.getRepairCost());
//						dictionary.append(", ");
//					}
//				}
//
//			} catch (ClassCastException e) {
//				e.printStackTrace();
//			}
//			
//			// 1行に書き込む
//			if (!dictionary.toString().equals(""))
//				stream.printf("%s[%d] = {%s}\n", varN, se.getID().getValue(), dictionary.toString());
//		}
	}
	
	/**
	 * ログを出力するファイルのストリームを取得する
	 * @param file
	 * @param append
	 * @return ログの出力先
	 */
	protected PrintStream getPrintStream(File file, boolean append) {
		PrintStream stream = null;
		try {
			if (append) {
				stream = new PrintStream(new FileOutputStream(file, true), false, "UTF-8");
			}
			else {
				stream = new PrintStream(file, "UTF-8");
			}
		} catch (FileNotFoundException e) {
			System.err.println("ファイルを開くまたは生成することができません");
			e.printStackTrace();
		} catch (SecurityException e) {
			System.err.println("ファイルに書き込む権限がありません");
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			System.err.println("UTF-8がサポートされていません");
			e.printStackTrace();
		}
		return stream;
	}

	/**
	 * オブジェクトの詳細な文字列表現を生成<br>
	 * 接続前に利用するとぬるぽの恐れがあります
	 * @param obj
	 * @return そのエンティティについての詳細な文字列
	 */
	public static String createDetailString(Object obj) {
		StringBuffer result = new StringBuffer();
		try {
			result.append(obj.toString() + "\n");
			if (obj instanceof Human) {
				Human human = (Human) obj;
				if (human.isHPDefined())
					result.append("\tHP:" + human.getHP() + "\n");
				if (human.isDamageDefined())
					result.append("\tDamage:" + human.getDamage() + "\n");
				if (human.isStaminaDefined())
					result.append("\tStamina:" + human.getStamina() + "\n");
				if (human.isBuriednessDefined())
					result.append("\tBuriedness:" + human.getBuriedness() + "\n");
				if (human.isDirectionDefined())
					result.append("\tDirection:" + human.getDirection() + "\n");
				if (human.isPositionDefined())
					result.append("\tPosition:" + human.getPosition() + "\n");
				if (human.isPositionHistoryDefined())
					result.append("\tPositionHistory:" + human.getPositionHistoryProperty() + "\n");
				if (human.isTravelDistanceDefined())
					result.append("\tPosition:" + human.getTravelDistance() + "\n");
				if (human.isXDefined())
					result.append("\tX:" + human.getX() + "\n");
				if (human.isYDefined())
					result.append("\tY:" + human.getY() + "\n");

				if (obj instanceof FireBrigade) {
					FireBrigade fb = (FireBrigade) obj;
					if (fb.isWaterDefined())
						result.append("\tWater:" + fb.getWater() + "\n");
				}

			} else if (obj instanceof Area) {
				Area area = (Area) obj;
				if (area.isBlockadesDefined()) {
					for (EntityID b : area.getBlockades()) {
						result.append("\tBlockades:" + staticModel.getEntity(b).toString() + "\n");
					}
				}
				
				if (obj instanceof Building) {
					Building building = (Building) obj;
					if (building.isBrokennessDefined())
						result.append("\tBrokenness:" + building.getBrokenness() + "\n");
					if (building.isBuildingAttributesDefined())
						result.append("\tBuildingAttributes:" + building.getBuildingAttributes() + "\n");
					if (building.isBuildingCodeDefined())
						result.append("\tBuildingCode:" + building.getBuildingCode() + "\n");
					if (building.isFierynessDefined())
						result.append("\tFieryness:" + building.getFieryness() + "\n");
					if (building.isFloorsDefined())
						result.append("\tFloors:" + building.getFloors() + "\n");
					if (building.isGroundAreaDefined())
						result.append("\tGroundArea:" + building.getGroundArea() + "\n");
					if (building.isIgnitionDefined())
						result.append("\tIgnition:" + building.getIgnition() + "\n");
					if (building.isImportanceDefined())
						result.append("\tImportance:" + building.getImportance() + "\n");
					if (building.isOnFire())
						result.append("\tisOnFire:" + building.isOnFire() + "\n");
					if (building.isTemperatureDefined())
						result.append("\tTemperatire:" + building.getTemperature() + "\n");
					if (building.isTotalAreaDefined())
						result.append("\tTotalArea:" + building.getTotalArea() + "\n");
				}
				else if (obj instanceof Road) {
					//Road road = (Road) obj;
				}
			}
			else if (obj instanceof Blockade) {
				Blockade blockade = (Blockade) obj;
				if (blockade.isRepairCostDefined())
					result.append("\tRepairCost"
							+ blockade.getRepairCost() + "\n");
				if (blockade.isApexesDefined()) {
					int[] apx = blockade.getApexes();
					if (apx.length > 1) {
						for (int i = 0; i < apx.length; i += 2) {
							result.append("\tApexes[" + (i / 2) + "]: (" + apx[i] + ", " + apx[i+1] + ")\n"); 
						}
					}
				}
				if (blockade.isPositionDefined())
					result.append("\tPosition:" + blockade.getPosition() + "\n");
				if (blockade.isXDefined())
					result.append("\tX:" + blockade.getX() + "\n");
				if (blockade.isYDefined())
					result.append("\tY:" + blockade.getY() + "\n");
			}

		}
		catch (ClassCastException e) {
			e.printStackTrace();
		}
		catch (NullPointerException e) {
			// System.err.println("perhaps before connecting ...");
			// e.printStackTrace();
		}
		return result.toString();
	}

	/**
	 * 詳細な文字列を取得する
	 * @param id
	 * @return そのエンティティについての詳細な文字列
	 */
	public static String createDetailString(EntityID id) {
		return createDetailString(staticModel.getEntity(id));
	}

	/**
	 * コンソールに詳細に描画する
	 * @param obj
	 *            描画対象
	 */
	public static void printObject(Object obj) {
		System.out.print(createDetailString(obj));
	}
}
