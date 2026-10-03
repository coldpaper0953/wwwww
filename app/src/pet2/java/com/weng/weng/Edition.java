package com.weng.weng;

/** 版本差异（pet2 = 桌宠2.0）：无人设；宠物形象可自定义。 */
public class Edition {

    /** 是否允许用户自定义宠物形象（设置页入口是否出现） */
    public static final boolean CUSTOM_SKIN = true;

    /** 自更新 Release 的 tag 前缀（桌宠2.0 tag 形如 pet2-v2.0.0） */
    public static final String RELEASE_TAG_PREFIX = "pet2-v";

    /** 自更新来源仓库 */
    public static final String GITHUB_REPO = "coldpaper0953/wwwww";

    /**
     * 桌宠 2.0 不带内置角色人设 —— 返回空串，请求里不注入任何角色设定。
     * （只保留一句中性的宠物口吻兜底，见 PetService.NEUTRAL_SYSTEM）
     */
    public static String persona() {
        return "";
    }
}
