package app;

import geometry.tools.LogUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import model.Model;
import view.View;
import view.mapDrawing.MapRenderer;

import com.jme3.bullet.BulletAppState;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;

import controller.Controller;
import controller.battlefield.BattlefieldController;
import controller.editor.EditorController;
import controller.ground.GroundController;

public class MainRTS extends OpenRTSApplication implements ActionListener {

	View view;
	MapRenderer tr;
	BattlefieldController fieldCtrl;
	EditorController editorCtrl;
	GroundController groundCtrl;
	Controller actualCtrl;

	public static void main(String[] args) {
		OpenRTSApplication.main(new MainRTS());
	}

	@Override
	public void simpleInitApp() {
		bulletAppState = new BulletAppState();
		stateManager.attach(bulletAppState);
		bulletAppState.getPhysicsSpace().setGravity(new Vector3f(0, 0, -1));
		// stateManager.detach(bulletAppState);

		flyCam.setUpVector(new Vector3f(0, 0, 1));
		flyCam.setEnabled(false);

		view = new View(rootNode, guiNode, bulletAppState.getPhysicsSpace(), assetManager, viewPort);

		NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);

		fieldCtrl = new BattlefieldController(view, niftyDisplay.getNifty(), inputManager, cam);
		fieldCtrl.addListener(this);
		editorCtrl = new EditorController(view, niftyDisplay.getNifty(), inputManager, cam);
		editorCtrl.addListener(this);
		groundCtrl = new GroundController(view, inputManager, cam);
		groundCtrl.addListener(this);

		niftyDisplay.getNifty().setIgnoreKeyboardEvents(true);
		// TODO: validation is needed to be sure everyting in XML is fine. see http://wiki.jmonkeyengine.org/doku.php/jme3:advanced:nifty_gui_best_practices
		// niftyDisplay.getNifty().validateXml("interface/screen.xml");
		niftyDisplay.getNifty().fromXml("interface/screen.xml", "editor");

		actualCtrl = editorCtrl;
		stateManager.attach(actualCtrl);
		actualCtrl.setEnabled(true);

		view.mapRend.renderTiles();

		guiViewPort.addProcessor(niftyDisplay);
	}

	@Override
	public void simpleUpdate(float tpf) {
		float maxedTPF = Math.min(tpf, 0.1f);
		listener.setLocation(cam.getLocation());
		listener.setRotation(cam.getRotation());
		view.actorManager.render();
		actualCtrl.update(maxedTPF);
		Model.updateConfigs();
	}

	@Override
	public void destroy() {
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Controller desiredCtrl;
		switch (e.getActionCommand()) {
			case "CTRL1":
				desiredCtrl = fieldCtrl;
				break;
			case "CTRL2":
				desiredCtrl = editorCtrl;
				break;
			case "CTRL3":
				desiredCtrl = groundCtrl;
				break;
			default:
				throw new IllegalAccessError();
		}
		LogUtil.logger.info("switching controller to " + desiredCtrl.getClass().getSimpleName());

		stateManager.detach(actualCtrl);
		actualCtrl.setEnabled(false);
		actualCtrl = desiredCtrl;
		stateManager.attach(actualCtrl);
		actualCtrl.setEnabled(true);

	}
}
