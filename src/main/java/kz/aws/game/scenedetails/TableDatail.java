package kz.aws.game.scenedetails;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.dispetcher.GameDispetcher;
import kz.aws.game.engine.model.ClueData;
import kz.aws.game.scenelist.SceneController;
import kz.aws.game.scenelist.SceneInfo;
import kz.aws.game.utils.SceneSettingsParser;
import kz.aws.game.utils.SceneSettingsParser.SceneSettings;
import kz.aws.game.utils.UiConfigParser;
import kz.aws.game.utils.UiFactory;

public class TableDatail extends StackPane implements Serializable, DialogPanel {

	private static final long serialVersionUID = 7773260755333927053L;
	private ImageView imageView;
	private Text dialogueText;
	private TextFlow dialogueTextFlow;
	private boolean usingTextFlow = false;
	private Text NameText;
	private HBox menu = new HBox(40);
	private BorderPane borderPane = new BorderPane();

	private StackPane tablePane; // Контейнер для таблички (или градиента) и текста
	/** Цвет имени персонажа в инлайн-стиле; участвует в binding стиля NameText. */
	private final StringProperty nameColorStyle = new SimpleStringProperty("");
	private transient Scene sceneRef;
	private transient SceneSettings settingsRef;

	@SuppressWarnings({ "static-access" })
	public TableDatail(AppSettings appSettings, Scene scene, GameDispetcher gameDispatcher, Stage primaryStage,
			Serializable objectToSave, int Clicker, int className) {
		SceneSettings settings = SceneSettingsParser.getSettings();
		this.sceneRef = scene;
		this.settingsRef = settings;
		SceneInfo.setHboxButton(menu);

		dialogueText = new Text("");
		NameText = new Text("");

		// Кнопки из Buttons.xml по контексту game-panel; действия через ButtonActionRegistry
		for (UiConfigParser.ButtonConfig btnCfg : UiConfigParser.getButtonsByContext("game-panel")) {
			Button btn = UiFactory.createButtonFromConfig(btnCfg, appSettings);
			menu.getChildren().add(btn);
			if (!settings.useGradientPanel) appSettings.ButtonStyle(btn, menu);
		}
		menu.setAlignment(Pos.CENTER);

		// Настройки стилей (базовые)
        dialogueText.getStyleClass().add("dialog-text");
        NameText.getStyleClass().add("speaker-name");

        // Установка отступов для текста (ТЕНИ)
		DropShadow dropShadow = new DropShadow();
		dropShadow.setRadius(2.0);
		dropShadow.setOffsetX(2.0);
		dropShadow.setOffsetY(0.5);
		dropShadow.setColor(Color.WHITE);
		dialogueText.setEffect(dropShadow);
		NameText.setEffect(dropShadow);

        // === ВЫБОР РЕЖИМА: ГРАДИЕНТ ИЛИ КАРТИНКА ===
        tablePane = new StackPane();
        
        if (settings.useGradientPanel) {
            // === РЕЖИМ ГРАДИЕНТА (НОВЫЙ) ===
            
            // Размеры панели
            tablePane.prefWidthProperty().bind(scene.widthProperty());
            tablePane.prefHeightProperty().bind(scene.heightProperty().multiply(settings.gradientPanelHeightMultiplier));
            
            // ВАЖНО: Ограничиваем максимальную высоту, чтобы StackPane не растягивал панель на весь экран
            tablePane.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
            
            // Стиль (Градиент)
            String style = String.format("-fx-background-color: linear-gradient(to bottom, %s, %s);", 
                settings.gradientStartColor, settings.gradientEndColor);
            tablePane.setStyle(style);
            
            // Контейнер контента: используем StackPane для гибкого позиционирования
            StackPane contentStack = new StackPane();
            
            // --- Текстовый блок (Слева-Сверху) ---
            VBox textContainer = new VBox(5);
            textContainer.setAlignment(Pos.TOP_LEFT);

            dialogueTextFlow = new TextFlow();
            dialogueTextFlow.getStyleClass().add("dialog-text");
            dialogueTextFlow.setVisible(false);
            dialogueTextFlow.setManaged(false);

            textContainer.getChildren().addAll(NameText, dialogueText, dialogueTextFlow);

            // Привязки размеров шрифта; цвет имени добавляется через nameColorStyle
            dialogueText.styleProperty().bind(Bindings.concat("-fx-font-size: ", scene.heightProperty().multiply(settings.dialogFontSizeMultiplier).asString(), "px;"));
            NameText.styleProperty().bind(Bindings.concat("-fx-font-size: ", scene.heightProperty().multiply(settings.nameFontSizeMultiplier).asString(), "px; ", nameColorStyle));

            // Ограничение ширины текста
            dialogueText.wrappingWidthProperty().bind(scene.widthProperty().multiply(settings.textContentWidth));
            dialogueTextFlow.prefWidthProperty().bind(scene.widthProperty().multiply(settings.textContentWidth));
            
            // Отступы текста
            StackPane.setMargin(textContainer, new Insets(
                scene.getHeight() * settings.textPaddingTop, 
                0, 
                0, 
                scene.getWidth() * settings.textPaddingLeft
            ));
            
            // --- Кнопки (Меню) (Снизу-По центру) ---
            menu.setAlignment(Pos.BOTTOM_CENTER); // Было CENTER, стало BOTTOM_CENTER
            menu.setSpacing(scene.getWidth() * settings.buttonSpacingMultiplier);
            
            // Отступы кнопок (чтобы не прилипали к самому низу)
            StackPane.setMargin(menu, new Insets(
                0, 
                0, 
                scene.getHeight() * 0.05, // Жесткий отступ снизу 5% для гарантии привязки
                0
            ));
            
            // Добавляем элементы в стек
            contentStack.getChildren().addAll(textContainer, menu);
            StackPane.setAlignment(textContainer, Pos.TOP_LEFT);
            StackPane.setAlignment(menu, Pos.BOTTOM_CENTER); // Явная привязка к низу
            
            // Добавляем контент в панель
            tablePane.getChildren().add(contentStack);
            StackPane.setAlignment(contentStack, Pos.CENTER);
            
        } else {
            // === РЕЖИМ КАРТИНКИ (СТАРЫЙ) ===
            
    		imageView = new ImageView(new Image(settings.tableImagePath));
    		imageView.setSmooth(true);
    		imageView.setFitWidth(scene.getHeight() * settings.tableWidthMultiplier);
    		imageView.setFitHeight(scene.getWidth() * settings.tableHeightMultiplier);
    		
    		// Привязываем размеры к картинке
    		tablePane.maxWidthProperty().bind(imageView.fitWidthProperty());
    		tablePane.maxHeightProperty().bind(imageView.fitHeightProperty());
    		
    		// Добавляем фон
    		tablePane.getChildren().add(imageView);
    		StackPane.setAlignment(imageView, Pos.CENTER);
            
            // Текст
    		VBox textContainer = new VBox(5);
    		textContainer.setAlignment(Pos.TOP_LEFT);
    		if (dialogueTextFlow == null) {
    		    dialogueTextFlow = new TextFlow();
    		    dialogueTextFlow.getStyleClass().add("dialog-text");
    		    dialogueTextFlow.setVisible(false);
    		    dialogueTextFlow.setManaged(false);
    		}
    		textContainer.getChildren().addAll(NameText, dialogueText, dialogueTextFlow);
    		textContainer.setMaxHeight(VBox.USE_PREF_SIZE);

            // Шрифты; цвет имени добавляется через nameColorStyle
            dialogueText.styleProperty().bind(Bindings.concat("-fx-font-size: ", scene.heightProperty().multiply(settings.dialogFontSizeMultiplier).asString(), "px;"));
            NameText.styleProperty().bind(Bindings.concat("-fx-font-size: ", scene.heightProperty().multiply(settings.nameFontSizeMultiplier).asString(), "px; ", nameColorStyle));

            // Wrapping
    		dialogueText.wrappingWidthProperty().bind(scene.widthProperty().multiply(settings.textContentWidth));
    		dialogueTextFlow.prefWidthProperty().bind(scene.widthProperty().multiply(settings.textContentWidth));
    		
    		// Добавляем текст
    		tablePane.getChildren().add(textContainer);
    		StackPane.setAlignment(textContainer, Pos.TOP_LEFT);
    		
    		// Отступы текста
    		StackPane.setMargin(textContainer, new Insets(
    		    scene.getHeight() * settings.textPaddingTop,
    		    0, 
    		    0, 
    		    scene.getWidth() * settings.textPaddingLeft
    		));
            
            // Меню (в старом режиме оно добавляется отдельно в borderPane)
    		menu.setAlignment(Pos.CENTER); 
    		menu.setSpacing(scene.getWidth() * settings.buttonSpacingMultiplier);
    		menu.setPadding(new Insets(0, scene.getWidth() * settings.buttonPaddingMultiplier, 0, scene.getWidth() * settings.buttonPaddingMultiplier));
    		
    		// В старом режиме кнопки были под табличкой, здесь мы их настроим в borderPane
            borderPane.setMargin(menu, new Insets(0, 0, scene.getHeight() * settings.menuBottomMargin, 0));
            borderPane.setBottom(menu); 
        }

		// Размеры кнопок (общие для обоих режимов)
		for (javafx.scene.Node node : menu.getChildren()) {
		    if (node instanceof Button) {
		        Button btn = (Button) node;
		        btn.prefWidthProperty().unbind();
		        btn.prefHeightProperty().unbind();
		        
		        if (settings.useGradientPanel) {
                    // === СТИЛЬ КНОПОК ДЛЯ ГРАДИЕНТА (CSS) ===
                    btn.getStyleClass().add("gradient-panel-button");
                    
                    // Привязываем размеры кнопки к размерам экрана (Динамические размеры)
                    btn.prefWidthProperty().bind(scene.widthProperty().multiply(settings.buttonWidthMultiplier)); 
                    btn.prefHeightProperty().bind(scene.heightProperty().multiply(settings.buttonHeightMultiplier));
                    
                    // Размер шрифта привязываем динамически к высоте экрана
                    btn.styleProperty().bind(Bindings.concat(
                        "-fx-font-size: ", scene.heightProperty().multiply(settings.buttonFontSizeMultiplier).asString(), "px;"
                    ));

                    // Анимация увеличения при наведении (остается в коде для плавности)
                    btn.setOnMouseEntered(e -> {
                        ScaleTransition st = new ScaleTransition(Duration.millis(200), btn);
                        st.setToX(1.2);
                        st.setToY(1.2);
                        st.play();
                    });
                    
                    btn.setOnMouseExited(e -> {
                        ScaleTransition st = new ScaleTransition(Duration.millis(200), btn);
                        st.setToX(1.0);
                        st.setToY(1.0);
                        st.play();
                    });
                    
                } else {
                    // === СТАРЫЙ СТИЛЬ ===
    		        btn.prefWidthProperty().bind(scene.widthProperty().multiply(settings.buttonWidthMultiplier)); 
    		        btn.prefHeightProperty().bind(scene.heightProperty().multiply(settings.buttonHeightMultiplier));
    		        btn.styleProperty().bind(Bindings.concat("-fx-font-size: ", scene.heightProperty().multiply(settings.buttonFontSizeMultiplier).asString(), "px;"));
                }
		    }
		}
		
		borderPane.prefWidthProperty().bind(scene.widthProperty());
		borderPane.prefHeightProperty().bind(scene.heightProperty());
		
        // В режиме картинки меню добавляется в borderPane. В режиме градиента - внутрь tablePane.
        // Чтобы не было дублирования, borderPane используется только если useGradientPanel = false (или для других целей).
        // Если useGradientPanel = true, то menu уже добавлено в contentBox.
	}
	
	private StackPane container; 
	
	public void addToScene(StackPane root) {
	    container = new StackPane();
        
        // В зависимости от режима, структура меняется
        SceneSettings settings = SceneSettingsParser.getSettings();
        
        if (settings.useGradientPanel) {
            // В режиме градиента tablePane содержит и текст, и кнопки. BorderPane не нужен (или пуст).
            container.getChildren().add(tablePane);
            StackPane.setAlignment(tablePane, Pos.BOTTOM_CENTER);
        } else {
            // В старом режиме используем borderPane для кнопок
            container.getChildren().addAll(tablePane, borderPane);
            
            // Отступ снизу для таблички
    		StackPane.setMargin(tablePane, new Insets(0,0, root.getHeight() * settings.tableBottomMargin, 0));
            StackPane.setAlignment(tablePane, Pos.BOTTOM_CENTER);
        }

	    // Сдвигаем контейнер вниз за пределы видимой области
	    container.setTranslateY(root.getHeight());

	    // Добавляем контейнер в root
	    root.getChildren().add(container);

	    // Переносим его на передний план
	    container.toFront();

	    // Анимация
	    TranslateTransition slideUp = new TranslateTransition();
	    slideUp.setDuration(Duration.millis(500)); 
	    slideUp.setNode(container);                
	    slideUp.setFromY(root.getHeight());        
	    slideUp.setToY(0);                         
	    slideUp.play();
	}

	public void removeFromScene(StackPane root) {
	    TranslateTransition slideDown = new TranslateTransition(Duration.millis(600), container);
	    slideDown.setFromY(0);
	    slideDown.setToY(root.getHeight());

	    slideDown.setOnFinished(e -> {
	        root.getChildren().remove(container);
	    });
	    slideDown.play();
	}

	/**
	 * Скрывает или показывает панель с текстом и кнопками без удаления из сцены.
	 * Используется при серии чёрных экранов (DimOverlay), чтобы между ними не мелькала панель.
	 */
	public void setPanelVisible(boolean visible) {
	    if (container != null) container.setVisible(visible);
	}

	private String fullText;
	private int currentIndex;
	private Timeline timeline;

	public void changeDialogText(String text, boolean animate) {
        if (timeline != null) {
            timeline.stop();
        }
        // Переключаемся обратно на обычный Text если был TextFlow
        if (usingTextFlow) {
            dialogueTextFlow.setVisible(false);
            dialogueTextFlow.setManaged(false);
            dialogueTextFlow.getChildren().clear();
            dialogueText.setVisible(true);
            dialogueText.setManaged(true);
            usingTextFlow = false;
        }
		fullText = text;

        if (!animate) {
            dialogueText.setText(fullText);
            return;
        }

		currentIndex = 0;
		timeline = new Timeline();
		timeline.getKeyFrames().add(new KeyFrame(Duration.millis(10), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (currentIndex <= fullText.length()) {
					String displayText = fullText.substring(0, currentIndex);
					dialogueText.setText(displayText);
					currentIndex++;
				} else {
					timeline.stop();
				}
			}
		}));
		timeline.setCycleCount(fullText.length() + 1);
		timeline.play();
	}

	// ==================== CLUE RENDERING ====================

	private static class DialogSegment {
	    final String text;
	    final ClueData clue; // null для обычного текста
	    DialogSegment(String text, ClueData clue) {
	        this.text = text;
	        this.clue = clue;
	    }
	}

	/**
	 * Отображает текст с кликабельными уликами через TextFlow.
	 */
	public void changeDialogTextWithClues(String text, List<ClueData> clues, boolean animate) {
	    if (timeline != null) timeline.stop();

	    // Переключаемся на TextFlow
	    dialogueText.setVisible(false);
	    dialogueText.setManaged(false);
	    dialogueTextFlow.setVisible(true);
	    dialogueTextFlow.setManaged(true);
	    usingTextFlow = true;

	    fullText = text;

	    List<DialogSegment> segments = buildSegments(text, clues);

	    dialogueTextFlow.getChildren().clear();
	    List<Text> allNodes = new ArrayList<>();
	    DropShadow shadow = (DropShadow) dialogueText.getEffect();

	    for (DialogSegment seg : segments) {
	        Text node = new Text("");
	        node.getStyleClass().add("dialog-text");
	        if (sceneRef != null && settingsRef != null) {
	            node.styleProperty().bind(Bindings.concat(
	                "-fx-font-size: ",
	                sceneRef.heightProperty().multiply(settingsRef.dialogFontSizeMultiplier).asString(),
	                "px;"
	            ));
	        }
	        if (shadow != null) {
	            DropShadow copy = new DropShadow();
	            copy.setRadius(shadow.getRadius());
	            copy.setOffsetX(shadow.getOffsetX());
	            copy.setOffsetY(shadow.getOffsetY());
	            copy.setColor(shadow.getColor());
	            node.setEffect(copy);
	        }

	        if (seg.clue != null) {
	            node.getStyleClass().add("clue-word");
	            node.setUnderline(true);
	            node.setFill(Color.web("#fffacd"));
	            node.setCursor(Cursor.HAND);

	            final ClueData clueData = seg.clue;
	            final Text clueNode = node;

	            node.setOnMouseEntered(e -> {
	                if (!clueNode.getStyleClass().contains("clue-collected")) {
	                    clueNode.setFill(Color.web("#ffd700"));
	                }
	            });
	            node.setOnMouseExited(e -> {
	                if (!clueNode.getStyleClass().contains("clue-collected")) {
	                    clueNode.setFill(Color.web("#fffacd"));
	                }
	            });
	            node.setOnMouseClicked(e -> {
	                SceneController.setPlayerChoice(clueData.getKey(), clueData.getValue());
	                clueNode.getStyleClass().add("clue-collected");
	                clueNode.setFill(Color.web("#88ff88"));
	                clueNode.setUnderline(false);
	                clueNode.setCursor(Cursor.DEFAULT);
	                clueNode.setOnMouseClicked(null);
	                clueNode.setOnMouseEntered(null);
	                clueNode.setOnMouseExited(null);
	                e.consume();
	            });
	        }

	        allNodes.add(node);
	        dialogueTextFlow.getChildren().add(node);
	    }

	    if (!animate) {
	        for (int i = 0; i < segments.size(); i++) {
	            allNodes.get(i).setText(segments.get(i).text);
	        }
	        return;
	    }

	    // Анимация печати через все сегменты
	    int totalLength = 0;
	    for (DialogSegment s : segments) totalLength += s.text.length();
	    final int total = totalLength;
	    final List<DialogSegment> segs = segments;
	    final List<Text> nodes = allNodes;

	    currentIndex = 0;
	    timeline = new Timeline();
	    timeline.getKeyFrames().add(new KeyFrame(Duration.millis(10), event -> {
	        if (currentIndex <= total) {
	            int remaining = currentIndex;
	            for (int i = 0; i < segs.size(); i++) {
	                String segText = segs.get(i).text;
	                if (remaining <= segText.length()) {
	                    nodes.get(i).setText(segText.substring(0, remaining));
	                    for (int j = i + 1; j < nodes.size(); j++) {
	                        nodes.get(j).setText("");
	                    }
	                    break;
	                } else {
	                    nodes.get(i).setText(segText);
	                    remaining -= segText.length();
	                }
	            }
	            currentIndex++;
	        } else {
	            timeline.stop();
	        }
	    }));
	    timeline.setCycleCount(total + 1);
	    timeline.play();
	}

	private List<DialogSegment> buildSegments(String text, List<ClueData> clues) {
	    List<DialogSegment> segments = new ArrayList<>();
	    int lastEnd = 0;
	    for (ClueData clue : clues) {
	        int clueStart = text.indexOf(clue.getDisplayText(), lastEnd);
	        if (clueStart < 0) continue;
	        if (clueStart > lastEnd) {
	            segments.add(new DialogSegment(text.substring(lastEnd, clueStart), null));
	        }
	        int clueEnd = clueStart + clue.getDisplayText().length();
	        segments.add(new DialogSegment(text.substring(clueStart, clueEnd), clue));
	        lastEnd = clueEnd;
	    }
	    if (lastEnd < text.length()) {
	        segments.add(new DialogSegment(text.substring(lastEnd), null));
	    }
	    return segments;
	}

	// ==================== STYLE ====================

	public void updateStyle(String styleName) {
	    dialogueText.getStyleClass().removeIf(s -> !s.equals("dialog-text") && !s.equals("text"));
	    NameText.getStyleClass().removeIf(s -> !s.equals("speaker-name") && !s.equals("text"));

	    if (styleName != null && !styleName.isEmpty()) {
	        dialogueText.getStyleClass().add(styleName);
	        NameText.getStyleClass().add(styleName);
	    }

	    if (usingTextFlow && dialogueTextFlow != null) {
	        for (javafx.scene.Node node : dialogueTextFlow.getChildren()) {
	            if (node instanceof Text t) {
	                t.getStyleClass().removeIf(s ->
	                    !s.equals("dialog-text") && !s.equals("text")
	                    && !s.equals("clue-word") && !s.equals("clue-collected"));
	                if (styleName != null && !styleName.isEmpty()) {
	                    t.getStyleClass().add(styleName);
	                }
	            }
	        }
	    }
	}

	public void changeNameText(String text) {
		NameText.setText(text);
	}

	/**
	 * Sets the speaker name color. Uses nameColorStyle (part of style binding) so it overrides CSS.
	 * Cannot use setStyle() because NameText.styleProperty() is bound to font size.
	 */
	public void changeColorNameText(Color color) {
		if (color == null) {
			resetNameColor();
			return;
		}
		nameColorStyle.set("-fx-fill: " + colorToCssHex(color) + ";");
	}

	/** Resets name color to stylesheet default (binding part cleared). */
	public void resetNameColor() {
		nameColorStyle.set("");
	}

	private static String colorToCssHex(Color c) {
		int r = (int) Math.round(c.getRed() * 255);
		int g = (int) Math.round(c.getGreen() * 255);
		int b = (int) Math.round(c.getBlue() * 255);
		return String.format("#%02x%02x%02x", r, g, b);
	}
}
