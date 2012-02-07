/**
 * 
 */
package rione.viewer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import rione.viewer.component.AdvancedViewComponent;

/**
 * キーリスナーを統一するためのクラス<br>
 * コレを込みでクラスの関係みるとぐちゃぐちゃなので，
 * キー関係は最後に見るのがいいと思う
 * 
 * @author utisam
 *
 */
public class ViewerEventProcessor implements KeyListener {
	
	protected AdvancedViewComponent viewer = null;
	
	public ViewerEventProcessor(AdvancedViewComponent v) {
		viewer = v;
	}

	/* (非 Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_U:
				// [U]ndo
				viewer.resetZoom();
				break;
			case KeyEvent.VK_I:
				// Zoom[I]n
				viewer.zoomIn();
				break;
			case KeyEvent.VK_O:
				// Zoom[O]ut
				viewer.zoomOut();
				break;
			default:
				return;
		}
		viewer.repaint();
	}

	/* (非 Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyReleased(KeyEvent e) {
	}

	/* (非 Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyTyped(KeyEvent e) {
	}
}
