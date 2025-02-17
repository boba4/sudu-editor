package org.sudu.experiments.demo;

import org.sudu.experiments.math.Color;
import org.sudu.experiments.math.V4f;

public interface Colors {

  V4f scrollBarBody1 = Color.Cvt.fromRGBA(50, 50, 50, 100);
  V4f scrollBarBody2 = Color.Cvt.fromRGBA(80, 80, 80, 200);
  V4f toolbarBg = new Color("#3C3F41");
  V4f toolbarBorder = new Color("#616161");
  V4f toolbarSelectedBg = new Color("#4B6EAF");

  V4f findUsagesBg = new Color("#3C3F41");
  V4f findUsagesBorder = new Color("#616161");
  V4f findUsagesSelectedBg = new Color("#4B6EAF");
  V4f findusagesBgLight = IdeaCodeColors.Colors.editBgColorLight;

}
