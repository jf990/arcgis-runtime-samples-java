/*
 * Copyright 2018 Esri.
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

package com.esri.samples.displayinformation.sketch_on_map;

import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.SketchCreationMode;
import com.esri.arcgisruntime.mapping.view.SketchEditor;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class SketchOnMapSample extends Application {

  private MapView mapView;
  private SketchEditor sketchEditor;
//  private Button redoButton;
//  private Button undoButton;
//  private Button clearButton;
//  private Button doneButton;
//  private Button editButton;
  private GraphicsOverlay graphicsOverlay;
  private SimpleFillSymbol fillSymbol;
  private SimpleLineSymbol lineSymbol;
  private SimpleMarkerSymbol pointSymbol;

  @Override
  public void start(Stage stage) {

    try {
      // create stack pane and application scene
      StackPane stackPane = new StackPane();
      Scene scene = new Scene(stackPane);
      scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

      // set title, size, and add scene to stage
      stage.setTitle("Sketch on Map Sample");
      stage.setWidth(1000);
      stage.setHeight(700);
      stage.setScene(scene);
      stage.show();

      // create a map with a basemap and add it to the map view
      ArcGISMap map = new ArcGISMap(Basemap.Type.IMAGERY, 64.3286, -15.5314, 13 );
      mapView = new MapView();
      mapView.setMap(map);

      // create a graphics overlay for the graphics
      graphicsOverlay = new GraphicsOverlay();

      // add the graphics overlay to the map view
      mapView.getGraphicsOverlays().add(graphicsOverlay);

      // create a new sketch editor and add it to the map view
      sketchEditor = new SketchEditor();
      mapView.setSketchEditor(sketchEditor);

      // define symbols
      pointSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.SQUARE, 0xFFFF0000,20);
      lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFFFF8800, 4);
      fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.CROSS, 0x40FFA9A9, lineSymbol);

      // create a combo box for graphic options
      ComboBox<SketchCreationMode> sketchOptionsDropDown = new ComboBox<>();
      sketchOptionsDropDown.getItems().setAll(SketchCreationMode.values());
      sketchOptionsDropDown.setMaxWidth(Double.MAX_VALUE);

      // create buttons for user interaction
      Button undoButton            = new Button("Undo changes to sketch");
      Button redoButton            = new Button("Redo changes to sketch");
      Button clearButton           = new Button("Clear graphics overlay");
      Button doneButton            = new Button ("Save sketch to graphics overlay");
      Button editButton            = new Button("Edit sketch");

      undoButton.setMaxWidth(Double.MAX_VALUE);
      redoButton.setMaxWidth(Double.MAX_VALUE);
      clearButton.setMaxWidth(Double.MAX_VALUE);
      doneButton.setMaxWidth(Double.MAX_VALUE);
      editButton.setMaxWidth(Double.MAX_VALUE);

      // disable clear, undo, redo and edit buttons when starting application
      clearButton.setDisable(true);
      redoButton.setDisable(true);
      undoButton.setDisable(true);
      editButton.setDisable(true);
      sketchOptionsDropDown.setPromptText("    -- Select a graphic to sketch --");

      // save the sketch as a graphic, and store graphic to the graphics overlay
      doneButton.setOnAction(event -> {
        storeGraphic();
        clearButton.setDisable(false);
      });

      // clear the graphics overlay, and disable the clear button
      clearButton.setOnAction(event -> {
        graphicsOverlay.getGraphics().clear();
        clearButton.setDisable(true);
        redoButton.setDisable(true);
        undoButton.setDisable(true);
        editButton.setDisable(true);
      });

      // undo the last change made whilst sketching graphic
      undoButton.setOnAction(event -> {
        if (sketchEditor.canUndo()) {
          sketchEditor.undo();
        };
      });

      sketchOptionsDropDown.getSelectionModel().selectedItemProperty().addListener(o -> {

        // create a graphics draw option based on source type
        SketchCreationMode sketchCreationMode = sketchOptionsDropDown.getSelectionModel().getSelectedItem();

        try {
          switch (sketchCreationMode) {
            case POLYLINE:
              createPolyline();
              break;
            case POLYGON:
              createPolygon();
              break;
            case POINT:
              createPoint();
              break;
            case MULTIPOINT:
              createMultiPoint();
              break;
            case FREEHAND_LINE:
              createFreeHandLine();
              break;
            case FREEHAND_POLYGON:
              createFreeHandPolygon();
              break;
          }

        } catch (Exception e) {
          new Alert(Alert.AlertType.ERROR, "Error finding draw style").show();
        }

      });

      // create a control panel
      VBox controlsVBox = new VBox(6);
      controlsVBox.setBackground(new Background(new BackgroundFill(Paint.valueOf("rgba(0, 0, 0, 0.3)"),
              CornerRadii.EMPTY, Insets.EMPTY)));
      controlsVBox.setPadding(new Insets(10));
      controlsVBox.setMaxSize(240, 110);
      controlsVBox.getStyleClass().add("panel-region");
      controlsVBox.getChildren().addAll(sketchOptionsDropDown, undoButton, redoButton, clearButton, doneButton, editButton);

      // add the map view to the stack pane
      stackPane.getChildren().addAll(mapView, controlsVBox);
      stackPane.setAlignment(controlsVBox, Pos.TOP_RIGHT);
      stackPane.setMargin(controlsVBox, new Insets(10, 10, 0, 10));


    } catch (Exception e) {

      e.printStackTrace();
    }
  }

  private void createPolyline() {
    sketchEditor.start(SketchCreationMode.POLYLINE);
  }

  private void createPolygon() { sketchEditor.start(SketchCreationMode.POLYGON);
  }

  private void createPoint() {
    sketchEditor.start(SketchCreationMode.POINT);
  }

  private void createMultiPoint() {
    sketchEditor.start(SketchCreationMode.MULTIPOINT);
  }

  private void createFreeHandLine() {
    sketchEditor.start(SketchCreationMode.FREEHAND_LINE);
  }

  private void createFreeHandPolygon() {
    sketchEditor.start(SketchCreationMode.FREEHAND_POLYGON);
  }

  /**
   * When the done button is clicked, check that sketch is valid. If so, get the geometry from the sketch, set its
   * symbol and add it to the graphics overlay.
   */
  private void storeGraphic() {
    if (!sketchEditor.isSketchValid()) {
      sketchEditor.stop();
      return;
    }

    // get the geometry from sketch editor
    Geometry sketchGeometry = sketchEditor.getGeometry();
    sketchEditor.stop();

    if (sketchGeometry != null) {

      // create a graphic from the sketch editor geometry
      Graphic graphic = new Graphic(sketchGeometry);

      // assign a symbol based on geometry type
      if (graphic.getGeometry().getGeometryType() == GeometryType.POLYGON) {
        graphic.setSymbol(fillSymbol);
      } else if (graphic.getGeometry().getGeometryType() == GeometryType.POLYLINE) {
        graphic.setSymbol(lineSymbol);
      } else if (graphic.getGeometry().getGeometryType() == GeometryType.POINT ||
          graphic.getGeometry().getGeometryType() == GeometryType.MULTIPOINT) {
        graphic.setSymbol(pointSymbol);
      }

      // add the graphic to the graphics overlay
      graphicsOverlay.getGraphics().add(graphic);
      undoButton.setDisable(false);

    }
  }



  /**
   * Stops and releases all resources used in application
   */
  @Override
  public void stop() {
    // release resources when the application closes
    if (mapView != null) {
      mapView.dispose();
    }
  }

  /**
   * Opens and runs application.
   *
   * @param args arguments passed to this application
   */
  public static void main(String[] args) {

    Application.launch(args);
  }
}
