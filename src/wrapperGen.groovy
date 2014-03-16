#!/usr/bin/env groovy
//indicate this is groovy script. groovy should be reachable from command line
def cli = new CliBuilder(
    usage: 'wrapperGen.groovy <-j pathToJar> <-c packagePath,classPath>'
)

import org.apache.commons.cli.Option

cli.with {
  h(longOpt: 'help', 'Usage information', required: false)
  j(longOpt:'jar', 'source jar file full path', args:1, required: true)
  c(longOpt: 'classes', 'package path or full path class name to generate', args:Option.UNLIMITED_VALUES, valueSeparator: ",", required: true) //this parameter accept multi value separate by comma
}

def opt = cli.parse(args)

if(!opt) return

//opt.c will read first parameter only. to read all parameters as array, use opt.cs format. 's' indicate multi values
def classes = opt.cs

classes.each{c -> println(c)}

