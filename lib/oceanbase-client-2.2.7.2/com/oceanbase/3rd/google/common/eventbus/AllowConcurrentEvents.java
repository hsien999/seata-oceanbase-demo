// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.3rd.google.common.eventbus;

import com.oceanbase.3rd.google.common.annotations.Beta;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Annotation;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Beta
public @interface AllowConcurrentEvents {
}
