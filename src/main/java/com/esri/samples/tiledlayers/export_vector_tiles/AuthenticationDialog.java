/*
 * Copyright 2017 Esri.
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

package com.esri.samples.tiledlayers.export_vector_tiles;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import com.esri.arcgisruntime.security.Credential;
import com.esri.arcgisruntime.security.OAuthConfiguration;
import com.esri.arcgisruntime.security.UserCredential;

/**
 * Custom dialog for getting a UserCredential.
 */
class AuthenticationDialog extends Dialog<UserCredential> {

  @FXML private TextField username;
  @FXML private PasswordField password;
  @FXML private ButtonType cancelButton;
  @FXML private ButtonType continueButton;

  AuthenticationDialog() {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/export_vector_tiles_auth_dialog.fxml"));
    loader.setRoot(this);
    loader.setController(this);

    setTitle("Authenticate");

    try {
      loader.load();
    } catch (Exception e) {
      e.printStackTrace();
    }

    setResultConverter(dialogButton -> {
      if (dialogButton == continueButton) {
        try {
          return new UserCredential(username.getText(), password.getText());
        } catch (Exception e) {
          Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
          alert.show();
        }
      }
      return null;
    });
  }

}
