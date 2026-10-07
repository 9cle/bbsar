package mchorse.bbs_mod.cubic.data.animation;

import mchorse.bbs_mod.data.IMapSerializable;
import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.data.types.ListType;
import mchorse.bbs_mod.data.types.MapType;
import mchorse.bbs_mod.data.types.StringType;
import mchorse.bbs_mod.math.Constant;
import mchorse.bbs_mod.math.molang.MolangParser;
import mchorse.bbs_mod.math.molang.expressions.MolangExpression;
import mchorse.bbs_mod.math.molang.expressions.MolangValue;

public class AnimationPart implements IMapSerializable
{
    public AnimationChannel position = new AnimationChannel();
    public AnimationChannel scale = new AnimationChannel();
    public AnimationChannel rotation = new AnimationChannel();

    private MolangParser parser;

    public AnimationPart(MolangParser parser)
    {
        this.parser = parser;
    }

    @Override
    public void fromData(MapType data)
    {
        if (data.has("translate")) parseChannel(this.position, data.get("translate"), MolangParser.ZERO);
        if (data.has("scale")) parseChannel(this.scale, data.get("scale"), MolangParser.ONE);
        if (data.has("rotate")) parseChannel(this.rotation, data.get("rotate"), MolangParser.ZERO);
    }

    private void parseChannel(AnimationChannel channel, BaseType data, MolangExpression defaultValue)
    {
        if (BaseType.isList(data))
        {
            for (BaseType keyframe : (ListType) data)
            {
                channel.keyframes.add(this.parseAnimationVector(keyframe, defaultValue));
            }

            channel.sort();
        }
    }

    private AnimationVector parseAnimationVector(BaseType data, MolangExpression defaultValue)
    {
        ListType values = (ListType) data;
        AnimationVector vector = new AnimationVector();

        if (values.size() >= 5)
        {
            vector.time = values.getDouble(0);
            vector.interp = AnimationInterpolation.byName(values.getString(1));
            vector.x = this.parseValue(this.parser, values.get(2), defaultValue);
            vector.y = this.parseValue(this.parser, values.get(3), defaultValue);
            vector.z = this.parseValue(this.parser, values.get(4), defaultValue);
        }

        return vector;
    }

    private MolangExpression parseValue(MolangParser parser, BaseType element, MolangExpression defaultValue)
    {
        if (element.isNumeric())
        {
            return new MolangValue(parser, new Constant(element.asNumeric().doubleValue()));
        }

        try
        {
            return parser.parseExpression(((StringType) element).value);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return defaultValue;
    }

    @Override
    public void toData(MapType data)
    {
        data.put("translate", this.serializeChannel(this.position));
        data.put("scale", this.serializeChannel(this.scale));
        data.put("rotate", this.serializeChannel(this.rotation));
    }

    private ListType serializeChannel(AnimationChannel channel)
    {
        ListType list = new ListType();

        for (AnimationVector keyframe : channel.keyframes)
        {
            ListType keyframeList = new ListType();

            keyframeList.addDouble(keyframe.time);
            keyframeList.addString(AnimationInterpolation.toName(keyframe.interp));
            keyframeList.add(keyframe.x.toData());
            keyframeList.add(keyframe.y.toData());
            keyframeList.add(keyframe.z.toData());

            list.add(keyframeList);
        }

        return list;
    }
}
