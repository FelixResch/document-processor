package org.web25.felix.jobs

import kotlin.reflect.KClass

class ConfigNotPresentException(kClass: KClass<*>) : RuntimeException("No config of type $kClass found")