/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

import com.mstest.CommonUtils
import com.mstest.EventHubProcesser

String functionName = "eventhubtrigger-verify"
String storageName = "mavenverifycihub"
String namespaceName = "FunctionCIEventHubNamespace-verify"
String resourceGroupName = "eventhubtrigger-verify-group"

EventHubProcesser eventHubProcesser = null
try {
    eventHubProcesser = new EventHubProcesser(resourceGroupName, namespaceName, storageName);
    eventHubProcesser.createOrGetEventHubByName("trigger")
    eventHubProcesser.createOrGetEventHubByName("output")
    // Get connnection string of EventHub and set it to trigger function
    def connectionString = eventHubProcesser.getEventHubConnectionString()
    CommonUtils.executeCommand("az functionapp config appsettings set --name ${functionName} --resource-group ${resourceGroupName} --settings CIEventHubConnection=\"${connectionString}\"")
    // verify
    CommonUtils.runVerification(new Runnable() {
        @Override
        void run() {
            eventHubProcesser.sendMessageToEventHub("trigger", "CIInput")
            sleep(10 * 1000 /* ms */)
            assert eventHubProcesser.getMessageFromEventHub("output").get(0) == "CITest"
        }
    })
} finally {
    if (eventHubProcesser != null) {
        eventHubProcesser.close()
    }
}
return true