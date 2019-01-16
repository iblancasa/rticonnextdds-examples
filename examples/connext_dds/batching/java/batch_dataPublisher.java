/*******************************************************************************
 (c) 2005-2014 Copyright, Real-Time Innovations, Inc.  All rights reserved.
 RTI grants Licensee a license to use, modify, compile, and create derivative
 works of the Software.  Licensee has the right to distribute object form only
 for use with RTI products.  The Software is provided "as is", with no warranty
 of any type, including any warranty for fitness for any purpose. RTI is under
 no obligation to maintain or support the Software.  RTI shall not be liable for
 any incidental or consequential damages arising out of the use or inability to
 use the software.
 ******************************************************************************/

/* batch_dataPublisher.java

   A publication of data of type batch_data

   This file is derived from code automatically generated by the rtiddsgen 
   command:

   rtiddsgen -language java -example <arch> .idl

   Example publication of type batch_data automatically generated by 
   'rtiddsgen' To test them follow these steps:

   (1) Compile this file and the example subscription.

   (2) Start the subscription with the command
       java batch_dataSubscriber <domain_id> <sample_count>
       
   (3) Start the publication with the command
       java batch_dataPublisher <domain_id> <sample_count>

   (4) [Optional] Specify the list of discovery initial peers and 
       multicast receive addresses via an environment variable or a file 
       (in the current working directory) called NDDS_DISCOVERY_PEERS.  
       
   You can run any number of publishers and subscribers programs, and can 
   add and remove them dynamically from the domain.
              
   Example:
        
       To run the example application on domain <domain_id>:
            
       Ensure that $(NDDSHOME)/lib/<arch> is on the dynamic library path for
       Java.                       
       
        On Unix: 
             add $(NDDSHOME)/lib/<arch> to the 'LD_LIBRARY_PATH' environment
             variable
                                         
        On Windows:
             add %NDDSHOME%\lib\<arch> to the 'Path' environment variable
                        

       Run the Java applications:
       
        java -Djava.ext.dirs=$NDDSHOME/class batch_dataPublisher <domain_id>

        java -Djava.ext.dirs=$NDDSHOME/class batch_dataSubscriber <domain_id>        

       
       
modification history
------------ -------         
*/

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import com.rti.dds.domain.*;
import com.rti.dds.infrastructure.*;
import com.rti.dds.publication.*;
import com.rti.dds.topic.*;
import com.rti.ndds.config.*;

// ===========================================================================

public class batch_dataPublisher {
    // -----------------------------------------------------------------------
    // Public Methods
    // -----------------------------------------------------------------------
    
    public static void main(String[] args) {
        // --- Get domain ID --- //
        int domainId = 0;
        if (args.length >= 1) {
            domainId = Integer.valueOf(args[0]).intValue();
        }

        // --- Get if the turbo mode should be enabled --- //
        int turbo_mode_on = 0;
        if (args.length >= 2) {
            turbo_mode_on = Integer.valueOf(args[1]).intValue();
        }
        
        // -- Get max loop count; 0 means infinite loop --- //
        int sampleCount = 0;
        if (args.length >= 3) {
            sampleCount = Integer.valueOf(args[2]).intValue();
        }
        
        /* Uncomment this to turn on additional logging
        Logger.get_instance().set_verbosity_by_category(
            LogCategory.NDDS_CONFIG_LOG_CATEGORY_API,
            LogVerbosity.NDDS_CONFIG_LOG_VERBOSITY_STATUS_ALL);
        */
        
        // --- Run --- //
        publisherMain(domainId, sampleCount, turbo_mode_on);
    }
    
    
    
    // -----------------------------------------------------------------------
    // Private Methods
    // -----------------------------------------------------------------------
    
    // --- Constructors: -----------------------------------------------------
    
    private batch_dataPublisher() {
        super();
    }
    
    
    // -----------------------------------------------------------------------
    
    private static void publisherMain(int domainId, int sampleCount, 
            int turbo_mode_on) {

        DomainParticipant participant = null;
        Publisher publisher = null;
        Topic topic = null;
        batch_dataDataWriter writer = null;
        String profile_name = null;
        String library_name = "batching_Library";

        try {
            // --- Create participant --- //
    
            /* We pick the profile name if the turbo_mode is selected or not.
             * If Turbo_mode is not selected, the batching profile will be used.
             */
            if (turbo_mode_on == 1) {
                profile_name = "turbo_mode_profile";
                System.out.println("Turbo Mode enable");
            } else {
                profile_name = "batch_profile";
                System.out.println("Manual batching enable");
            }
            
            /* To customize participant QoS, use
               the configuration file
               USER_QOS_PROFILES.xml */
    
            participant = DomainParticipantFactory.TheParticipantFactory.
                create_participant_with_profile(
                    domainId, library_name, profile_name,
                    null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (participant == null) {
                System.err.println("create_participant error\n");
                return;
            }        
                    
            // --- Create publisher --- //
    
            /* To customize publisher QoS, use
               the configuration file USER_QOS_PROFILES.xml */
    
            publisher = participant.create_publisher_with_profile(
                library_name, profile_name, null /* listener */,
                StatusKind.STATUS_MASK_NONE);
            if (publisher == null) {
                System.err.println("create_publisher error\n");
                return;
            }                   
                
        
            // --- Create topic --- //

            /* Register type before creating topic */
            String typeName = batch_dataTypeSupport.get_type_name();
            batch_dataTypeSupport.register_type(participant, typeName);
    
            /* To customize topic QoS, use
               the configuration file USER_QOS_PROFILES.xml */
    
            topic = participant.create_topic(
                "Example batch_data",
                typeName, DomainParticipant.TOPIC_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (topic == null) {
                System.err.println("create_topic error\n");
                return;
            }           
                
            // --- Create writer --- //
    
            /* To customize data writer QoS, use
               the configuration file USER_QOS_PROFILES.xml */
    
            writer = (batch_dataDataWriter)
                publisher.create_datawriter_with_profile(
                    topic, library_name, profile_name,
                    null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (writer == null) {
                System.err.println("create_datawriter error\n");
                return;
            }           
                                        
            // --- Write --- //

            /* Create data sample for writing */
            batch_data instance = new batch_data();

            InstanceHandle_t instance_handle = InstanceHandle_t.HANDLE_NIL;
            /* For a data type that has a key, if the same instance is going to
               be written multiple times, initialize the key here
               and register the keyed instance prior to writing */
            //instance_handle = writer.register_instance(instance);

            long sendPeriodMillis = 1 * 1000; // 4 seconds
            if (turbo_mode_on == 1) {
                sendPeriodMillis = 0;
            }

            for (int count = 0;
                 (sampleCount == 0) || (count < sampleCount);
                 ++count) {
                System.out.println("Writing batch_data, count " + count);

                /* Modify the instance to be written here */
                instance.x = count;
            
                /* Write data */
                writer.write(instance, instance_handle);
                try {
                    Thread.sleep(sendPeriodMillis);
                } catch (InterruptedException ix) {
                    System.err.println("INTERRUPTED");
                    break;
                }
            }

            //writer.unregister_instance(instance, instance_handle);

        } finally {

            // --- Shutdown --- //

            if(participant != null) {
                participant.delete_contained_entities();

                DomainParticipantFactory.TheParticipantFactory.
                    delete_participant(participant);
            }
            /* RTI Connext provides finalize_instance()
               method for people who want to release memory used by the
               participant factory singleton. Uncomment the following block of
               code for clean destruction of the participant factory
               singleton. */
            //DomainParticipantFactory.finalize_instance();
        }
    }
}

        