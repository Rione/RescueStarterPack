package rione.viewer.component.controller;

import java.awt.Color;
import java.awt.Dimension;

import rescuecore2.worldmodel.EntityID;
import rione.viewer.component.AdvancedViewComponent;

/**
 * ビューア全体に必要なデータを管理するインタフェース<br>
 * コントローラだけど参照される感じ<br>
 * 真の意味でコントロールしてるのはViewerEventProcessorかも
 * これに沿って作れば新しいコントローラを作ることができる<br>
 * コンストラクタでこれを引数にしたコントローラ(コピーコンストラクタ)を作れば引継ぎができる<br>
 * 新しい表示を作りたいときはココにオプションを作ってから，
 * 全部のコントローラにCtrl+1でメソッドを自動生成させると速い
 * 
 * @author utisam
 *
 */
public interface Controller {
	
	/** コントロールしているAdvancedViewComponent */
	AdvancedViewComponent getViewComponent();
	/** フォーカスされたEntityID */
	EntityID getFocus();
	/**
	 * フォーカスのsetter<br>
	 * 主にEntityをクリックした時の呼び出し用
	 */
	void setFocus(EntityID id);
	/**
	 * 毎サイクル呼び出される
	 * @param time
	 * @return 次のサイクルからのコントローラ(通常はthis)
	 */
	Controller update(int time);
	/** 視界内のEntityの表示をするか */
	boolean visibleEntity();
	/** 自由設定装飾を表示するか */
	boolean costomExtention();
	/** 自由描画を表示するか */
	boolean customRender();
	/** 呼び出された命令を表示するか */
	boolean lastCommand();
	/** worldmodelの座標をプロットする */
	boolean plotLocation();
	/** 追従 */
	boolean followFocus();
	/** 終了するか */
	boolean killAgents();
	/**
	 * 保存するフォルダ
	 * @return 保存しないならnull
	 */
	String getSaveFolderName();
	/** フォルダに画像を保存するか */
	boolean saveImage();
	/** 独自ログを保存するか */
	boolean saveLog();
	/** 市民の色(nullならデフォルト) */
	Color getCivilianColor();
	/** ATの色(nullならデフォルト) */
	Color getAmbulanceTeamColor();
	/** FBの色(nullならデフォルト) */
	Color getFireBrigadeColor();
	/** PFの色(nullならデフォルト) */
	Color getPoliceForceColor();
	Dimension getFrameSize();
}
