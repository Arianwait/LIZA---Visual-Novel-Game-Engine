package kz.aws.game.character;

import java.io.Serializable;

import javafx.scene.image.ImageView;
import kz.aws.game.appsettings.DefaultAppSettings;
import kz.aws.game.appsettings.AppSettings;

public abstract class Character implements Serializable, ICharacter {

    protected String inputPersonName;
    protected String personName;
    private static final long serialVersionUID = 4308108538268418783L;
    protected transient ImageView characterImageView;
    protected AppSettings appSettings = new DefaultAppSettings();
    private int reputathion = 50;

    @Override
    public ImageView getCharacterImageView() {
        return characterImageView;
    }

    @Override
    public void setCharacterImageView(ImageView view) {
        this.characterImageView = view;
    }

    @Override
    public String getName() {
        return inputPersonName;
    }

    @Override
    public int getReputathion() {
        return reputathion;
    }

    @Override
    public void setReputathion(int value) {
        this.reputathion = Math.max(0, Math.min(100, value));
    }
}
