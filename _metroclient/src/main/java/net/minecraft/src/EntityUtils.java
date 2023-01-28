package net.minecraft.src;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;

public class EntityUtils {
	
    public static int getEntityAge(EntityLivingBase elb)
    {
        return elb.entityAge;
    }

    public static void setEntityAge(EntityLivingBase elb, int age)
    {
        elb.entityAge = age;
    }

    public static void despawnEntity(EntityLiving el)
    {
        el.despawnEntity();
    }
}
