package com.joshiminh.wgcinema.dashboard.agents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.joshiminh.wgcinema.data.AgeRatingColor;
import com.joshiminh.wgcinema.data.DAO;
import com.joshiminh.wgcinema.utils.*;

import static com.joshiminh.wgcinema.utils.AgentStyles.*;

@SuppressWarnings("unused")
public class MovieAgent extends JFrame {
  private String[] movieColumns;
  private final String databaseUrl;
  private final boolean isNewMovie;
  private final int movieId;
  private final List<JComponent> inputComponents;
  private final Runnable refreshCallback;

  public MovieAgent(String url, int id, boolean newMovie, Runnable refreshCallback) {
      databaseUrl = url;
      movieId = id;
      isNewMovie = newMovie;
      inputComponents = new ArrayList<>();
      this.refreshCallback = refreshCallback;

      setDefaultCloseOperation(DISPOSE_ON_CLOSE);
      setIconImage(ResourceUtil.loadAppIcon());
      applyFrameDefaults(this, isNewMovie ? "Add New Movie" : "Edit Movie", 800, 825);

      setupFrame();
  }

  private void setupFrame() {
      loadMovieData();
      add(createHeaderPanel(), BorderLayout.NORTH);
      add(createFormPanel(), BorderLayout.CENTER);
      add(createFooterPanel(), BorderLayout.SOUTH);
  }

  private JPanel createHeaderPanel() {
      JPanel headerPanel = new JPanel(new BorderLayout());
      headerPanel.setBorder(new EmptyBorder(20, 0, 10, 0));
      headerPanel.setBackground(PRIMARY_BACKGROUND);

      JLabel titleLabel = createHeaderLabel(isNewMovie ? "Add New Movie" : "Edit Movie");
      headerPanel.add(titleLabel, BorderLayout.CENTER);

      return headerPanel;
  }

  private JPanel createFormPanel() {
      JPanel formContainer = new JPanel(new GridBagLayout());
      formContainer.setBackground(PRIMARY_BACKGROUND);
      formContainer.setBorder(new EmptyBorder(10, FORM_PADDING, 10, FORM_PADDING));

      JPanel formPanel = new JPanel(new GridBagLayout());
      formPanel.setBackground(PRIMARY_BACKGROUND);

      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = LABEL_INSETS;
      gbc.anchor = GridBagConstraints.WEST;

      String[] columnValues = loadColumnValues();

      for (int i = 0; i < movieColumns.length; i++) {
          JLabel label = createFormLabel(movieColumns[i]);
          gbc.gridx = 0;
          gbc.gridy = i;
          gbc.weightx = 0.25;
          gbc.fill = GridBagConstraints.NONE;
          formPanel.add(label, gbc);

          JComponent inputComponent = createInputComponent(movieColumns[i], columnValues[i]);
          inputComponents.add(inputComponent);
          gbc.gridx = 1;
          gbc.weightx = 0.75;
          gbc.fill = GridBagConstraints.HORIZONTAL;
          formPanel.add(inputComponent, gbc);
      }

      GridBagConstraints formGbc = new GridBagConstraints();
      formGbc.fill = GridBagConstraints.BOTH;
      formGbc.weightx = 1;
      formGbc.weighty = 1;
      formContainer.add(formPanel, formGbc);

      return formContainer;
  }

  private String[] loadColumnValues() {
      String[] columnValues = new String[movieColumns.length];
      if (isNewMovie) {
          for (int i = 0; i < movieColumns.length; i++) {
              if (movieColumns[i].equalsIgnoreCase("id")) {
                  columnValues[i] = "";
              } else {
                  columnValues[i] = "";
              }
          }
      } else {
          try (ResultSet resultSet = DAO.fetchMovieDetails(databaseUrl, movieId)) {
              if (resultSet.next()) {
                  for (int i = 0; i < movieColumns.length; i++) {
                      columnValues[i] = resultSet.getString(movieColumns[i]);
                  }
              }
          } catch (SQLException e) {
              showErrorDialog("Error loading movie details: " + e.getMessage());
          }
      }
      return columnValues;
  }

  private JLabel createFormLabel(String columnName) {
      JLabel label = new JLabel(columnName.replace("_", " ") + ":");
      label.setForeground(TEXT_COLOR);
      label.setFont(LABEL_FONT);
      label.setHorizontalAlignment(SwingConstants.RIGHT);
      return label;
  }

  private JPanel createFooterPanel() {
      JPanel footerPanel = new JPanel();
      footerPanel.setBorder(new EmptyBorder(10, 0, 20, 0));
      footerPanel.setBackground(PRIMARY_BACKGROUND);

      JButton saveButton = new JButton(isNewMovie ? "Add Movie" : "Save Changes");
      styleButton(saveButton);
      saveButton.addActionListener(e -> performSaveAction());
      footerPanel.add(saveButton);

      return footerPanel;
  }

  private void loadMovieData() {
      movieColumns = getColumnNames(databaseUrl, "movies");
  }

  private static String[] getColumnNames(String url, String tableName) {
      try (Connection connection = DriverManager.getConnection(url)) {
          DatabaseMetaData metaData = connection.getMetaData();
          ResultSet resultSet = metaData.getColumns(null, null, tableName, null);
          List<String> columnNames = new ArrayList<>();
          while (resultSet.next()) {
              columnNames.add(resultSet.getString("COLUMN_NAME"));
          }
          return columnNames.toArray(new String[0]);
      } catch (SQLException e) {
          e.printStackTrace();
          return new String[0];
      }
  }

  private JComponent createInputComponent(String fieldName, String defaultValue) {
      return switch (fieldName.toLowerCase()) {
          case "age_rating" -> createRatingComboBox(defaultValue);
          case "release_date" -> createDateSpinner(defaultValue);
          case "description" -> createDescriptionTextArea(defaultValue);
          default -> createTextField(fieldName, defaultValue);
      };
  }

  private JComboBox<String> createRatingComboBox(String defaultValue) {
      JComboBox<String> comboBox = new JComboBox<>(AgeRatingColor.getRatings());
      comboBox.setSelectedItem(defaultValue);
      styleComponent(comboBox);
      comboBox.setPreferredSize(new Dimension(0, 30));
      return comboBox;
  }

  private JSpinner createDateSpinner(String defaultValue) {
      JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
      JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
      dateSpinner.setEditor(dateEditor);
      styleComponent(dateSpinner);
      try {
          if (!defaultValue.isEmpty()) {
              dateSpinner.setValue(java.sql.Date.valueOf(defaultValue));
          }
      } catch (IllegalArgumentException ignored) {}
      dateSpinner.setPreferredSize(new Dimension(0, 30));
      return dateSpinner;
  }

  private JScrollPane createDescriptionTextArea(String defaultValue) {
      JTextArea textArea = new JTextArea(defaultValue, 4, 20);
      textArea.setLineWrap(true);
      textArea.setWrapStyleWord(true);
      styleComponent(textArea);
      textArea.setCaretColor(TEXT_COLOR);

      JScrollPane scrollPane = new JScrollPane(textArea);
      scrollPane.setPreferredSize(new Dimension(0, 150));
      return scrollPane;
  }

  private JTextField createTextField(String fieldName, String defaultValue) {
      JTextField textField = new JTextField(defaultValue);
      styleComponent(textField);
      textField.setBorder(componentBorder());
      textField.setCaretColor(TEXT_COLOR);
      textField.setEditable(!fieldName.equalsIgnoreCase("id"));

      if (fieldName.equalsIgnoreCase("id") && isNewMovie) {
          textField.setText("Auto-generated by DB");
          textField.setForeground(LIGHT_TEXT_COLOR);
      }
      textField.setPreferredSize(new Dimension(0, 30));
      return textField;
  }

  private void performSaveAction() {
      if (isNewMovie) {
          addNewMovie();
      } else {
          saveChanges();
      }
  }

  private void addNewMovie() {
      String[] allValues = extractValues(inputComponents);

      List<String> filteredColumns = new ArrayList<>();
      List<String> filteredValues = new ArrayList<>();

      for (int i = 0; i < movieColumns.length; i++) {
          if (!movieColumns[i].equalsIgnoreCase("id")) {
              filteredColumns.add(movieColumns[i]);
              filteredValues.add(allValues[i]);
          }
      }

      int result = DAO.insertMovie(databaseUrl, filteredColumns.toArray(new String[0]), filteredValues.toArray(new String[0]));

      showResultDialog(result > 0 ? "Movie added successfully!" : "Failed to add movie", result > 0);
      if (result > 0 && refreshCallback != null) {
          refreshCallback.run();
      }
      dispose();
  }

  private void saveChanges() {
      String[] updatedValues = extractValues(inputComponents);
      int result = DAO.updateMovieById(databaseUrl, movieColumns, updatedValues, movieId);
      showResultDialog(result > 0 ? "Changes saved successfully!" : "No changes were made", result > 0);
      if (result > 0 && refreshCallback != null) {
          refreshCallback.run();
      }
      dispose();
  }

  private String[] extractValues(List<JComponent> components) {
      String[] values = new String[movieColumns.length];
      for (int i = 0; i < components.size(); i++) {
          JComponent component = components.get(i);
          if (component instanceof JTextField field) {
              values[i] = field.getText();
          } else if (component instanceof JComboBox<?> comboBox) {
              values[i] = comboBox.getSelectedItem().toString();
          } else if (component instanceof JSpinner spinner) {
              values[i] = new java.sql.Date(((java.util.Date) spinner.getValue()).getTime()).toString();
          } else if (component instanceof JScrollPane pane) {
              values[i] = ((JTextArea) pane.getViewport().getView()).getText();
          }
      }
      return values;
  }

  private void showResultDialog(String message, boolean success) {
      JOptionPane.showMessageDialog(this, message, success ? "Success" : "Warning",
              success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
  }

  private void showErrorDialog(String message) {
      JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
  }
}