package com.mysema.query.scala.sql

import com.mysema.query.codegen._
import com.mysema.codegen.CodeWriter
import com.mysema.query

import java.util._
import java.io.IOException

import scala.reflect.BeanProperty
import scala.collection.JavaConversions._

/**
 * @author tiwe
 *
 */
class ScalaBeanSerializer extends Serializer {
    
    val javaBeanSupport = true;
    
    val javadocSuffix = " is a Querydsl bean type";
    
    def serialize(model: EntityType, serializerConfig: SerializerConfig, writer: CodeWriter) {
        val simpleName: String = model.getSimpleName;
        
        // package
        if (!model.getPackageName.isEmpty()){
            writer.packageDecl(model.getPackageName);
        }
        
        // imports
        var importedClasses: Set[String] = getAnnotationTypes(model);
        importedClasses.add("scala.reflect.BeanProperty");
        if (model.hasLists()){
            importedClasses.add(classOf[List[_]].getName);
        }
        if (model.hasMaps()){
            importedClasses.add(classOf[Map[_,_]].getName);
        }
        
        writer.importClasses(importedClasses.toArray(new Array[String](0)):_*);
               
        // javadoc        
        writer.javadoc(simpleName + javadocSuffix);
        
        // header
        for (annotation <- model.getAnnotations){
            writer.annotation(annotation);
        }               
        
        writer.beginClass(model);
        
        // properties
        for (property <- model.getProperties()){
            property.getAnnotations.foreach( {writer.annotation(_);} )
            if (javaBeanSupport){
                writer.line("@BeanProperty");
            }            
            writer.publicField(property.getType(), property.getEscapedName, "_");
        }
                
        writer.end();
    }

    def getAnnotationTypes(model: EntityType): Set[String] = {
        var imports = new HashSet[String]();
//        for (annotation <- model.getAnnotations){
//            imports.add(annotation.annotationType.getName);
//        }
        imports.addAll(model.getAnnotations.map(_.annotationType.getName))
        // flatMap flattens the results of the map-function.
        // E.g. List(List(1,2,3), List(4,5,6)).flatMap(_.map(_*3)) ends up as List(3, 6, 9, 12, 15, 18).
        imports.addAll(model.getProperties.flatMap(_.getAnnotations.map(_.annotationType.getName)))
        
//        for (property <- model.getProperties; annotation <- property.getAnnotations) {
//            imports.add(annotation.annotationType.getName);
//        }
        imports;
    }
    
}