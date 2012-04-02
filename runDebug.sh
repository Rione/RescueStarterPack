#! /bin/bash

HOST="localhost"

if [ $# = 1 ]; then
	HOST=$1
fi

TEAM_CP="jars/clear.jar:\
jars/collapse.jar:\
jars/commons-logging-1.1.1.jar:\
jars/dom4j.jar:\
jars/gis2.jar:\
jars/handy.jar:\
jars/human.jar:\
jars/ignition.jar:\
jars/jaxen-1.1.1.jar:\
jars/jcommon-1.0.16.jar:\
jars/jfreechart-1.0.13.jar:\
jars/jscience-4.3.jar:\
jars/jsi-1.0b2p1.jar:\
jars/jts-1.11.jar:\
jars/junit-4.5.jar:\
jars/kernel.jar:\
jars/log4j-1.2.15.jar:\
jars/maps.jar:\
jars/misc.jar:\
jars/rescuecore.jar:\
jars/rescuecore2.jar:\
jars/resq-fire.jar:\
jars/standard.jar:\
jars/traffic3.jar:\
jars/trove-0.1.8.jar:\
jars/uncommons-maths-1.2.jar:\
jars/xml-0.0.6.jar:\
jars/rioneviewer.jar:\
bin"

java -Xms2048m -Xmx2048m -cp $TEAM_CP sample.LaunchAgents sample.agent.at.DebugAmbulanceTeam*n sample.agent.fb.DebugFireBrigade*n sample.agent.pf.DebugPoliceForce*n sample.agent.SampleCentre*n rione.viewer.AdvancedViewer*1 -h $HOST --loadabletypes.inspect.dir=jars --random.seed=1

