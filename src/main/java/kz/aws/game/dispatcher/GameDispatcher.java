package kz.aws.game.dispatcher;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.appsettings.JsonParser;
import kz.aws.game.engine.model.SceneFrame;
import kz.aws.game.engine.parser.ScenarioValidator;
import kz.aws.game.engine.parser.SceneXmlParser;
import kz.aws.game.mainscene.LogoAnimation;
import kz.aws.game.utils.VirtualViewport;
import kz.aws.game.utils.ResourceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Точка входа приложения. Создаёт Stage и Scene, разворачивает
 * {@link VirtualViewport}: весь интерфейс живёт в контейнере фиксированного
 * дизайн-разрешения и масштабируется под реальный размер окна одним трансформом.
 */
public class GameDispatcher extends Application implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(GameDispatcher.class);

	private static final long serialVersionUID = 8222725889624267118L;

	/** Аргумент запуска: проверить сценарий и выйти, не открывая окно. */
	private static final String VALIDATE_FLAG = "--validate";

	/**
	 * Точка входа. С аргументом {@value #VALIDATE_FLAG} проверяет сценарий
	 * и завершается с кодом 1, если найдены проблемы.
	 *
	 * @param args аргументы командной строки
	 */
	public static void main(String[] args) {
		// логи кладём рядом с игрой, а не в случайную рабочую директорию
		System.setProperty("liza.logs",
				ResourceLocator.resolve("logs").toString());

		if (args.length > 0 && VALIDATE_FLAG.equals(args[0])) {
			System.exit(validateScenario());
			return;
		}
		launch(args);
	}

	/**
	 * Разбирает сценарий и печатает найденные проблемы.
	 *
	 * @return 0 — сценарий корректен; 1 — есть проблемы
	 */
	private static int validateScenario() {
		Map<Integer, List<SceneFrame>> scenes = SceneXmlParser.parseAllScenes();
		List<String> problems = new java.util.ArrayList<>(ScenarioValidator.validate(scenes));
		problems.addAll(ScenarioValidator.validateCommands(SceneXmlParser.getScenarioPath()));

		LOG.info("Проверка сценария: сцен загружено — " + scenes.size());
		if (problems.isEmpty()) {
			LOG.info("Проблем не найдено.");
			return 0;
		}
		LOG.info("Найдено проблем: " + problems.size());
		for (String problem : problems) {
			LOG.info("  " + problem);
		}
		return 1;
	}

	/**
	 * Инициализирует окно, вьюпорт и стартовую анимацию логотипа.
	 * Размер окна не задаётся напрямую: Scene создаётся с размером игровой
	 * области из настроек, а Stage сам подгоняется под неё вместе с рамкой —
	 * так игровая область получается ровно 16:9 без полос летербокса.
	 *
	 * @param primaryStage основной Stage приложения
	 */
	@Override
	public void start(Stage primaryStage) {
		// без ресурсов игра покажет чёрный экран — сообщаем причину сразу
		ResourceLocator.exists("lib/Scene");

		primaryStage.getIcons().add(new Image(ResourceLocator.url("lib/Logo/logo.png")));
		primaryStage.setTitle("Innagano");
		primaryStage.setResizable(false);

		AppSettings appSettings = JsonParser.readConfig();
		appSettings.setGamedispetcher(this);
		appSettings.setStage(primaryStage);

		VirtualViewport viewport = new VirtualViewport();
		appSettings.setRoot(viewport.getContentRoot());

		Scene scene = new Scene(viewport.getScreenRoot(),
				appSettings.getWindowWidth(), appSettings.getWindowHeight());
		appSettings.setScene(scene);
		viewport.bindTo(scene);

		scene.getStylesheets().add(ResourceLocator.url("lib/config/style.css"));

		appSettings.getRoot().getChildren().add(new LogoAnimation(appSettings));

		primaryStage.setScene(scene);
		primaryStage.setFullScreen(appSettings.isFullscreen());
		primaryStage.setFullScreenExitHint("");
		primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
		primaryStage.show();
	}
}
