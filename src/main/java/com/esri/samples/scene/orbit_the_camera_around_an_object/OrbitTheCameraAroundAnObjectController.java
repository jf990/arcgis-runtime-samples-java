/*
 * Copyright 2019 Esri.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.esri.samples.scene.orbit_the_camera_around_an_object;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LayerSceneProperties;
import com.esri.arcgisruntime.mapping.view.OrbitGeoElementCameraController;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.symbology.ModelSceneSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;

import java.io.File;

public class OrbitTheCameraAroundAnObjectController {

  private Graphic plane3D;

  @FXML
  private Button travelAwayButton;
  @FXML
  private Button returnButton;
  @FXML
  private Button offsetButton;

  @FXML
  private CheckBox cameraHeadingCheckbox;
  @FXML
  private CheckBox planeAutoHeadingCheckbox;

  @FXML
  private OrbitGeoElementCameraController orbitCameraController;
  @FXML
  private SceneView sceneView;
  @FXML
  private Slider headingSlider;
  @FXML
  private Slider planeHeadingSlider;

  public void initialize() {

    try {

      // create a scene and add a basemap to it
      ArcGISScene scene = new ArcGISScene();
      scene.setBasemap(Basemap.createImagery());
      sceneView.setArcGISScene(scene);

      // add base surface for elevation data
      Surface surface = new Surface();
      ArcGISTiledElevationSource elevationSource = new ArcGISTiledElevationSource("http://elevation3d.arcgis" +
              ".com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer");
      surface.getElevationSources().add(elevationSource);
      scene.setBaseSurface(surface);

      // create a graphics overlay for the scene
      GraphicsOverlay sceneGraphicsOverlay = new GraphicsOverlay();
      sceneGraphicsOverlay.getSceneProperties().setSurfacePlacement(LayerSceneProperties.SurfacePlacement.ABSOLUTE);
      sceneView.getGraphicsOverlays().add(sceneGraphicsOverlay);

      // add rendered using rotation expressions
      SimpleRenderer renderer = new SimpleRenderer();
      renderer.getSceneProperties().setHeadingExpression("[HEADING]");
      renderer.getSceneProperties().setPitchExpression("[PITCH]");
      renderer.getSceneProperties().setRollExpression("[ROLL]");
      sceneGraphicsOverlay.setRenderer(renderer);

      // create a graphic with a ModelSceneSymbol of a plane to add to the scene
      String modelURI = new File("./samples-data/bristol/Collada/Bristol.dae").getAbsolutePath();
      ModelSceneSymbol plane3DSymbol = new ModelSceneSymbol(modelURI, 1.0);
      plane3DSymbol.loadAsync();
      plane3DSymbol.setHeading(45);
      plane3D = new Graphic(new Point(6.637, 45.399, 1955, SpatialReferences.getWgs84()), plane3DSymbol);
      sceneGraphicsOverlay.getGraphics().add(plane3D);

      // instantiate a new camera controller which orbits a given geo element at a certain distance
      orbitCameraController = new OrbitGeoElementCameraController(plane3D, 100.0);
      // set the orbit camera controller to the scene view
      sceneView.setCameraController(orbitCameraController);
      // set the starter value, and min max heading offset of the camera
      orbitCameraController.setCameraHeadingOffset(100);
      orbitCameraController.setMaxCameraHeadingOffset(110);
      orbitCameraController.setMinCameraHeadingOffset(10);
      // set the starter value, and min max pitch offset of the camera
      orbitCameraController.setCameraPitchOffset(85);
      orbitCameraController.setMaxCameraPitchOffset(80);
      orbitCameraController.setMinCameraPitchOffset(10);
      // set the starter value, and min and max camera distance to the target
      orbitCameraController.setCameraDistance(30);
      orbitCameraController.setMaxCameraDistance(30);
      orbitCameraController.setMinCameraDistance(10);
      // set the where the plane is positioned on the screen
      orbitCameraController.setTargetVerticalScreenFactor(0.3f);
      // set the target offset of the plane to 0 (the plane will remain the center focus of the camera controller)
      resetControllerTargetToPlane();

      // update slider positions whilst interacting with the camera
      sceneView.addViewpointChangedListener(event -> headingSlider.setValue(orbitCameraController.getCameraHeadingOffset()));

      // adjust the heading direction of the plane using the plane heading slider
      planeHeadingSlider.valueProperty().addListener(o -> plane3D.getAttributes().put("HEADING", planeHeadingSlider.getValue()));

      // set the camera's heading using the heading slider
      headingSlider.valueProperty().addListener(o -> orbitCameraController.setCameraHeadingOffset(headingSlider.getValue()));

    } catch (Exception e) {
      // on any exception, print the stack trace
      e.printStackTrace();
    }
  }

  /**
   * Animate the camera away from the plane to the specified location over a period of 4 seconds.
   */
  @FXML
  private void handleTravelAwayButtonClicked() {
    orbitCameraController.setTargetOffsetsAsync(-25, -25, 10, 3);
  }

  /**
   * Animate the camera back to the plane over a period of 4 seconds.
   */
  @FXML
  private void handleReturnButtonClicked() {
    resetControllerTargetToPlane();
  }

  /**
   * Set a target offset value for the camera to orbit round the tail of the plane.
   */
  @FXML
  private void handleOffsetButtonClicked() {
    orbitCameraController.setTargetOffsetY(-5);
    orbitCameraController.setTargetOffsetX(-5);
    orbitCameraController.setTargetOffsetZ(0);
  }

  /**
   * Set if the camera heading can be interacted with via external input (e.g. keyboard or mouse).
   */
  @FXML
  private void handleHeadingInteractionCheckBoxChanged() {
    orbitCameraController.setCameraHeadingOffsetInteractive(cameraHeadingCheckbox.isSelected());
    headingSlider.setDisable(cameraHeadingCheckbox.isSelected());
  }

  /**
   * Set if the camera will follow the plane heading.
   */
  @FXML
  private void handleAutoHeadingCheckBox() {
    orbitCameraController.setAutoHeadingEnabled(planeAutoHeadingCheckbox.isSelected());
  }

  private void resetControllerTargetToPlane() {
    orbitCameraController.setTargetOffsetX(0);
    orbitCameraController.setTargetOffsetY(0);
    orbitCameraController.setTargetOffsetZ(0);
  }

  /**
   * Disposes of application resources.
   */
  void terminate() {

    // release resources when the application closes
    if (sceneView != null) {
      sceneView.dispose();
    }
  }
}
