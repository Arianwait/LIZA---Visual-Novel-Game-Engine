package kz.aws.game.character;

import javafx.scene.image.ImageView;

/**
 * Интерфейс персонажа. Команды сценария ищутся по id в {@link kz.aws.game.gameaction.GameActionRegistry};
 * обработчики (gameaction) выполняют логику сами, используя этот API. Новое действие = новый класс с @GameAction.
 */
public interface ICharacter {

    ImageView getCharacterImageView();

    void setCharacterImageView(ImageView view);

    String getName();

    int getReputathion();

    void setReputathion(int value);
}
