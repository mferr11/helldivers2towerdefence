package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.ServiceLocator;

/** Render a static texture. */
public class TextureRenderComponentAlpha extends RenderComponent {
  private Texture texture;
  private float alphaValue;

  /**
   * @param texturePath Internal path of static texture to render. Will be scaled to the entity's
   *     scale.
   */
  public TextureRenderComponentAlpha(String texturePath, float alphaVal) {
    this(ServiceLocator.getResourceService().getAsset(texturePath, Texture.class));
    this.alphaValue = alphaVal;
  }

  // ...
  /**
   * @param texture Static texture to render. Will be scaled to the entity's scale.
   */
  public TextureRenderComponentAlpha(Texture texture) {
    this.texture = texture;
  }

  /** Scale the entity to a width of 1 and a height matching the texture's ratio */
  public void scaleEntity() {
    entity.setScale(1f, (float) texture.getHeight() / texture.getWidth());
  }

  public void setAlphaValue(float value) {
    alphaValue = value;
  }

  /**
   * Set a new texture to render
   *
   * @param texturePath Internal path of texture to render
   */
  public void setTexture(String texturePath) {
    this.texture = ServiceLocator.getResourceService().getAsset(texturePath, Texture.class);
  }

  /**
   * Set a new texture to render
   *
   * @param texture Texture to render
   */
  public void setTexture(Texture texture) {
    this.texture = texture;
  }

  @Override
  protected void draw(SpriteBatch batch) {
    Vector2 position = entity.getPosition();
    Vector2 scale = entity.getScale();
    batch.setColor(1, 1, 1, alphaValue);
    batch.draw(texture, position.x, position.y, scale.x, scale.y);
    batch.setColor(1, 1, 1, 1);
  }
}
