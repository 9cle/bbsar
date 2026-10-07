package mchorse.bbs_mod;

import mchorse.bbs_mod.film.tts.ValueVoiceColors;
import mchorse.bbs_mod.film.tts.ValueVoiceModel;
import mchorse.bbs_mod.settings.SettingsBuilder;
import mchorse.bbs_mod.settings.values.ValueBoolean;
import mchorse.bbs_mod.settings.values.ValueColors;
import mchorse.bbs_mod.settings.values.ValueEditorLayout;
import mchorse.bbs_mod.settings.values.ValueFloat;
import mchorse.bbs_mod.settings.values.ValueInt;
import mchorse.bbs_mod.settings.values.ValueLanguage;
import mchorse.bbs_mod.settings.values.ValueLink;
import mchorse.bbs_mod.settings.values.ValueOnionSkin;
import mchorse.bbs_mod.settings.values.ValueString;
import mchorse.bbs_mod.settings.values.ValueVideoSettings;
import mchorse.bbs_mod.utils.MathUtils;
import mchorse.bbs_mod.utils.colors.Colors;

public class BBSSettings
{
    public static ValueString version;

    public static ValueColors favoriteColors;
    public static ValueLanguage language;
    public static ValueInt primaryColor;
    public static ValueBoolean enableTrackpadIncrements;
    public static ValueInt userIntefaceScale;
    public static ValueInt tooltipStyle;
    public static ValueFloat fov;
    public static ValueBoolean hsvColorPicker;
    public static ValueBoolean forceQwerty;
    public static ValueBoolean freezeModels;

    public static ValueBoolean enableCursorRendering;
    public static ValueBoolean enableMouseButtonRendering;
    public static ValueBoolean enableKeystrokeRendering;
    public static ValueInt keystrokeOffset;
    public static ValueInt keystrokeMode;

    public static ValueLink backgroundImage;
    public static ValueInt backgroundColor;

    public static ValueInt scrollbarShadow;
    public static ValueInt scrollbarWidth;
    public static ValueFloat scrollingSensitivity;

    public static ValueBoolean multiskinMultiThreaded;

    public static ValueString videoEncoderPath;
    public static ValueBoolean videoEncoderLog;
    public static ValueVideoSettings videoSettings;

    public static ValueInt duration;
    public static ValueBoolean editorLoop;
    public static ValueInt editorJump;
    public static ValueBoolean editorDisplayPosition;
    public static ValueInt editorGuidesColor;
    public static ValueBoolean editorRuleOfThirds;
    public static ValueBoolean editorCenterLines;
    public static ValueBoolean editorCrosshair;
    public static ValueBoolean editorSeconds;
    public static ValueInt editorPeriodicSave;
    public static ValueBoolean editorHorizontalFlight;
    public static ValueEditorLayout editorLayoutSettings;
    public static ValueOnionSkin editorOnionSkin;

    public static ValueFloat recordingCountdown;
    public static ValueInt recordingNextVariable;

    public static ValueBoolean damageControl;

    public static ValueBoolean audioWaveformVisible;
    public static ValueInt audioWaveformDensity;
    public static ValueFloat audioWaveformWidth;
    public static ValueInt audioWaveformHeight;
    public static ValueBoolean audioWaveformFilename;
    public static ValueBoolean audioWaveformTime;

    public static ValueString elevenLabsToken;
    public static ValueBoolean elevenLabsAllVoices;
    public static ValueVoiceModel elevenVoiceModel;
    public static ValueVoiceColors elevenVoiceColors;

    public static int primaryColor()
    {
        return primaryColor(Colors.A50);
    }

    public static int primaryColor(int alpha)
    {
        return primaryColor.get() | alpha;
    }

    public static int getDefaultDuration()
    {
        return duration == null ? 30 : duration.get();
    }

    public static float getFov()
    {
        return BBSSettings.fov == null ? MathUtils.toRad(50) : MathUtils.toRad(BBSSettings.fov.get());
    }

    public static void register(SettingsBuilder builder)
    {
        builder.category("appearance");

        version = builder.getString("version", BBSMod.SIGNIFICANT_VERSION);
        version.invisible();

        language = new ValueLanguage("language");
        builder.register(language);
        primaryColor = builder.getInt("primary_color", Colors.ACTIVE).color();
        enableTrackpadIncrements = builder.getBoolean("trackpad_increments", true);
        userIntefaceScale = builder.getInt("ui_scale", 2, 0, 4);
        tooltipStyle = builder.getInt("tooltip_style", 1);
        fov = builder.getFloat("fov", 40, 0, 180);
        hsvColorPicker = builder.getBoolean("hsv_color_picker", true);
        forceQwerty = builder.getBoolean("force_qwerty", false);
        freezeModels = builder.getBoolean("freeze_models", false);

        favoriteColors = new ValueColors("favorite_colors");
        builder.register(favoriteColors);

        enableCursorRendering = builder.category("tutorials").getBoolean("cursor", false);
        enableMouseButtonRendering = builder.getBoolean("mouse_buttons", false);
        enableKeystrokeRendering = builder.getBoolean("keystrokes", false);
        keystrokeOffset = builder.getInt("keystrokes_offset", 10, 0, 20);
        keystrokeMode = builder.getInt("keystrokes_position", 1);

        backgroundImage = builder.category("background").getRL("image",  null);
        backgroundColor = builder.getInt("color",  Colors.A75).colorAlpha();

        scrollbarShadow = builder.category("scrollbars").getInt("shadow", Colors.A50).colorAlpha();
        scrollbarWidth = builder.getInt("width", 4, 2, 10);
        scrollingSensitivity = builder.getFloat("sensitivity", 1F, 0F, 10F);

        multiskinMultiThreaded = builder.category("multiskin").getBoolean("multithreaded", true);

        videoEncoderPath = builder.category("video").getString("encoder_path", "ffmpeg");
        videoEncoderLog = builder.getBoolean("log", true);
        builder.register(videoSettings = new ValueVideoSettings("settings"));

        /* Camera editor */
        duration = builder.category("editor").getInt("duration", 30, 1, 1000);
        editorJump = builder.getInt("jump", 5, 1, 1000);
        editorLoop = builder.getBoolean("loop", false);
        editorDisplayPosition = builder.getBoolean("position", false);
        editorGuidesColor = builder.getInt("guides_color", 0xcccc0000).colorAlpha();
        editorRuleOfThirds = builder.getBoolean("rule_of_thirds", false);
        editorCenterLines = builder.getBoolean("center_lines", false);
        editorCrosshair = builder.getBoolean("crosshair", false);
        editorSeconds = builder.getBoolean("seconds", false);
        editorPeriodicSave = builder.getInt("periodic_save", 60, 0, 3600);
        editorHorizontalFlight = builder.getBoolean("horizontal_flight", false);
        builder.register(editorLayoutSettings = new ValueEditorLayout("layout"));
        builder.register(editorOnionSkin = new ValueOnionSkin("onion_skin"));

        recordingCountdown = builder.category("recording").getFloat("countdown", 1.5F, 0F, 30F);
        recordingNextVariable = builder.getInt("next", 1, 1, 100);

        damageControl = builder.category("dc").getBoolean("enabled", true);

        builder.category("audio");
        audioWaveformVisible = builder.getBoolean("waveform_visible", true);
        audioWaveformDensity = builder.getInt("waveform_density", 20, 10, 100);
        audioWaveformWidth = builder.getFloat("waveform_width", 0.8F, 0F, 1F);
        audioWaveformHeight = builder.getInt("waveform_height", 24, 10, 40);
        audioWaveformFilename = builder.getBoolean("waveform_filename", false);
        audioWaveformTime = builder.getBoolean("waveform_time", false);

        builder.category("elevenlabs");
        elevenLabsToken = builder.getString("token", "");
        elevenLabsAllVoices = builder.getBoolean("all_voices", false);
        elevenVoiceModel = new ValueVoiceModel("model");
        elevenVoiceColors = new ValueVoiceColors("colors");

        builder.register(elevenVoiceModel);
        builder.register(elevenVoiceColors);
    }
}