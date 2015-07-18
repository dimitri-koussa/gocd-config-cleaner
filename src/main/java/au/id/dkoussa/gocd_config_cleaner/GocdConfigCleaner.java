package au.id.dkoussa.gocd_config_cleaner;

import nu.xom.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GocdConfigCleaner {

    private RandomWords randomWords;
    private Document document;
    private Map<String, String> agentResourceMapping = new HashMap<String, String>();
    private PipelineStageJobConfig pipelineStageJobConfig = new PipelineStageJobConfig();

    public Document clean(String gocdConfigFile, String serverId) throws Exception {
        Builder parser = new Builder();
        document = parser.build(new File(gocdConfigFile));

        scanAndReplaceTemplates();
        scanPipelineStageAndJobNames();

        replaceAgentResources("//cruise/agents/agent/resources/resource");
        replaceAgentResources("//cruise/pipelines/pipeline/stage/jobs/job/resources/resource");
        replaceAgentResources("//cruise/templates/pipeline/stage/jobs/job/resources/resource");

        replacePipelineStageJobNamesInFetchArtifacts("//cruise/pipelines/pipeline");
        replacePipelineStageJobNamesInFetchArtifacts("//cruise/templates/pipeline");
        replacePipelineOnMaterials("//cruise/pipelines/pipeline/materials/pipeline");

        replacePipelineStageAndJobNames();

        clearAttributes("//cruise/server/@*");
        clearAttributes("//cruise/pipelines/pipeline/materials/pipeline/@materialName");
        clearAttributes("//cruise/repositories/repository/@name");
        clearTextNodes("//cruise/repositories/repository/configuration/property/value");
        clearAttributes("//cruise/pipelines/@group");
        clearAttributes("//cruise/pipelines/pipeline/materials/git/@*");
        clearAttributes("//cruise/pipelines/pipeline/stage/jobs/job/tasks/exec/@command");
        clearAttributes("//cruise/pipelines/pipeline/stage/jobs/job/tasks/exec/@workingdir");
        clearTextNodes("//cruise/pipelines/pipeline/stage/jobs/job/tasks/exec/arg");
        clearAttributes("//cruise/pipelines/pipeline/stage/jobs/job/artifacts/artifact/@src");
        clearAttributes("//cruise/pipelines/pipeline/stage/jobs/job/artifacts/artifact/@dest");
        clearAttributes("//cruise/pipelines/pipeline/stage/jobs/job/tasks/fetchartifact/@srcdir");
        clearAttributes("//cruise/pipelines/pipeline/stage/jobs/job/tasks/fetchartifact/@dest");
        clearAttributes("//cruise/pipelines/pipeline/stage/jobs/job/tasks/fetchartifact/@srcfile");
        clearAttributes("//cruise/pipelines/pipeline/environmentvariables/variable/@name");
        clearTextNodes("//cruise/pipelines/pipeline/environmentvariables/variable/value");
        clearAttributes("//cruise/agents/agent/@hostname");
        clearAttributes("//cruise/agents/agent/@uuid");
        clearAttributesWithIpAddress("//cruise/agents/agent/@ipaddress");
        clearAttributes("//cruise/templates/pipeline/stage/jobs/job/tasks/exec/@command");
        clearTextNodes("//cruise/templates/pipeline/stage/jobs/job/tasks/exec/arg");
        clearAttributes("//cruise/templates/pipeline/stage/jobs/job/tasks/fetchartifact/@srcdir");
        clearAttributes("//cruise/templates/pipeline/stage/jobs/job/tasks/fetchartifact/@dest");

        setServerId(serverId);

        return document;
    }

    private void setServerId(String serverId) {
        Attribute serverIdAttribute = (Attribute) document.query("//cruise/server/@serverId").get(0);
        serverIdAttribute.setValue(serverId);
    }

    private class PipelineStageJobConfig {

        private Map<String, PipelineCfg> pipelines = new HashMap<String, PipelineCfg>();
        private Map<String, PipelineCfg> templates = new HashMap<String, PipelineCfg>();

        public void addPipeline(String pipelineName) {
            pipelines.put(pipelineName, new PipelineCfg());
        }

        public PipelineCfg getPipeline(String pipelineName) {
            return pipelines.get(pipelineName);
        }

        public void addTemplate(String template) {
            templates.put(template, new PipelineCfg());
        }

        public PipelineCfg getTemplate(String templateName) {
            return templates.get(templateName);
        }

        private class PipelineCfg {
            private String replacementName = randomWords.next();
            private Map<String, StageCfg> stages = new HashMap<String, StageCfg>();
            private PipelineCfg template;

            public void addStage(String stageName) {
                stages.put(stageName, new StageCfg());
            }

            public StageCfg getStage(String stageName) {
                return this.template == null ? stages.get(stageName) : template.getStage(stageName);
            }

            public void setTemplate(PipelineCfg template) {
                this.template = template;
            }
        }

        private class StageCfg {
            private String replacementName = randomWords.next();
            private Map<String, JobCfg> jobs = new HashMap<String, JobCfg>();

            public void addJob(String jobName) {
                jobs.put(jobName, new JobCfg());
            }

            public JobCfg getJob(String jobName) {
                return jobs.get(jobName);
            }

            @Override
            public String toString() {
                return replacementName;
            }
        }

        private class JobCfg {
            private String replacementName = randomWords.next();
        }
    }

    private void replacePipelineOnMaterials(String xpath) {
        Nodes pipelines = document.query(xpath);
        for (int i = 0; i < pipelines.size(); i++) {
            Element pipeline = (Element) pipelines.get(i);
            Attribute pipelineNameAttribute = pipeline.getAttribute("pipelineName");
            Attribute stageNameAttribute = pipeline.getAttribute("stageName");

            String pipelineName = pipelineNameAttribute.getValue();
            String stageName = stageNameAttribute.getValue();

            String newPipelineName = pipelineStageJobConfig.getPipeline(pipelineName).replacementName;
            String newStageName = pipelineStageJobConfig.getPipeline(pipelineName).getStage(stageName).replacementName;

            pipelineNameAttribute.setValue(newPipelineName);
            stageNameAttribute.setValue(newStageName);
        }
    }

    private void replacePipelineStageJobNamesInFetchArtifacts(String xpath) {
        Nodes pipelines = document.query(xpath);
        for (int i = 0; i < pipelines.size(); i++) {
            Element pipeline = (Element) pipelines.get(i);
            String pipelineName = pipeline.getAttribute("name").getValue();

            Nodes fetchartifacts = pipeline.query("stage/jobs/job/tasks/fetchartifact");
            for (int k = 0; k < fetchartifacts.size(); k++) {
                Element fetchartifact = (Element) fetchartifacts.get(k);

                String targetPipelineName = fetchartifact.getAttribute("pipeline").getValue();
                String targetStageName = fetchartifact.getAttribute("stage").getValue();
                String targetJobName = fetchartifact.getAttribute("job").getValue();

                if (targetPipelineName.equals("")) {
                    targetPipelineName = pipelineName;
                }

                String pipelineReplacement = pipelineStageJobConfig.getPipeline(targetPipelineName).replacementName;
                String stageReplacement = pipelineStageJobConfig.getPipeline(targetPipelineName).getStage(targetStageName).replacementName;
                String jobReplacement = pipelineStageJobConfig.getPipeline(targetPipelineName).getStage(targetStageName).getJob(targetJobName).replacementName;

                fetchartifact.getAttribute("pipeline").setValue(pipelineReplacement);
                fetchartifact.getAttribute("stage").setValue(stageReplacement);
                fetchartifact.getAttribute("job").setValue(jobReplacement);
            }
        }
    }

    private void scanAndReplaceTemplates() {
        Nodes pipelines = document.query("//cruise/templates/pipeline");
        for (int i = 0; i < pipelines.size(); i++) {
            Element template = (Element) pipelines.get(i);
            Attribute templateNameAttribute = template.getAttribute("name");
            String templateName = templateNameAttribute.getValue();
            pipelineStageJobConfig.addTemplate(templateName);
            templateNameAttribute.setValue(pipelineStageJobConfig.getTemplate(templateName).replacementName);

            Nodes stages = template.query("stage");
            for (int j = 0; j < stages.size(); j++) {
                Element stage = (Element) stages.get(j);
                Attribute stageNameAttribute = stage.getAttribute("name");
                String stageName = stageNameAttribute.getValue();
                pipelineStageJobConfig.getTemplate(templateName).addStage(stageName);
                stageNameAttribute.setValue(pipelineStageJobConfig.getTemplate(templateName).getStage(stageName).replacementName);

                Nodes jobs = stage.query("jobs/job");
                for (int k = 0; k < jobs.size(); k++) {
                    Element job = (Element) jobs.get(k);
                    Attribute jobNameAttribute = job.getAttribute("name");
                    String jobName = jobNameAttribute.getValue();
                    pipelineStageJobConfig.getTemplate(templateName).getStage(stageName).addJob(jobName);
                    jobNameAttribute.setValue(pipelineStageJobConfig.getTemplate(templateName).getStage(stageName).getJob(jobName).replacementName);
                }
            }
        }
    }

    private void scanPipelineStageAndJobNames() {
        Nodes pipelines = document.query("//cruise/pipelines/pipeline");
        for (int i = 0; i < pipelines.size(); i++) {
            Element pipeline = (Element) pipelines.get(i);
            String pipelineName = pipeline.getAttribute("name").getValue();
            pipelineStageJobConfig.addPipeline(pipelineName);

            Attribute templateAttribute = pipeline.getAttribute("template");
            if (templateAttribute != null) {
                PipelineStageJobConfig.PipelineCfg template = pipelineStageJobConfig.getTemplate(templateAttribute.getValue());
                pipelineStageJobConfig.getPipeline(pipelineName).setTemplate(template);
            }

            Nodes stages = pipeline.query("stage");
            for (int j = 0; j < stages.size(); j++) {
                Element stage = (Element) stages.get(j);
                String stageName = stage.getAttribute("name").getValue();
                pipelineStageJobConfig.getPipeline(pipelineName).addStage(stageName);

                Nodes jobs = stage.query("jobs/job");
                for (int k = 0; k < jobs.size(); k++) {
                    Element job = (Element) jobs.get(k);
                    String jobName = job.getAttribute("name").getValue();
                    pipelineStageJobConfig.getPipeline(pipelineName).getStage(stageName).addJob(jobName);
                }
            }
        }
    }

    private void replacePipelineStageAndJobNames() {
        Nodes pipelines = document.query("//cruise/pipelines/pipeline");
        for (int i = 0; i < pipelines.size(); i++) {
            Element pipeline = (Element) pipelines.get(i);
            Attribute pipelineAttribute = pipeline.getAttribute("name");
            String pipelineName = pipelineAttribute.getValue();
            pipelineAttribute.setValue(pipelineStageJobConfig.getPipeline(pipelineName).replacementName);

            Attribute templateAttribute = pipeline.getAttribute("template");
            if (templateAttribute != null) {
                templateAttribute.setValue(pipelineStageJobConfig.getTemplate(templateAttribute.getValue()).replacementName);
            } else {
                Nodes stages = pipeline.query("stage");
                for (int j = 0; j < stages.size(); j++) {
                    Element stage = (Element) stages.get(j);
                    Attribute stageAttribute = stage.getAttribute("name");
                    String stageName = stageAttribute.getValue();
                    stageAttribute.setValue(pipelineStageJobConfig.getPipeline(pipelineName).getStage(stageName).replacementName);

                    Nodes jobs = stage.query("jobs/job");
                    for (int k = 0; k < jobs.size(); k++) {
                        Element job = (Element) jobs.get(k);
                        Attribute jobAttribute = job.getAttribute("name");
                        String jobName = jobAttribute.getValue();
                        jobAttribute.setValue(pipelineStageJobConfig.getPipeline(pipelineName).getStage(stageName).getJob(jobName).replacementName);
                    }
                }
            }
        }
    }

    private void replaceAgentResources(String xpath) {
        Nodes nodes = document.query(xpath);
        for (int i = 0; i < nodes.size(); i++) {
            Element element = (Element) nodes.get(i);
            String agentResource = element.getValue();
            String sanitizedResource = agentResourceMapping.containsKey(agentResource) ? agentResourceMapping.get(agentResource) : randomWords.next();
            element.removeChildren();
            element.appendChild(sanitizedResource);
            agentResourceMapping.put(agentResource, sanitizedResource);
        }
    }

    private void clearTextNodes(String xpath) {
        Nodes nodes = document.query(xpath);
        for (int i = 0; i < nodes.size(); i++) {
            Element element = (Element) nodes.get(i);
            element.getChild(0).detach();
            element.appendChild(randomWords.next());
        }
    }

    private void clearAttributes(String xpath) throws Exception {
        Nodes nodes = document.query(xpath);
        for (int i = 0; i < nodes.size(); i++) {
            Attribute attribute = (Attribute) nodes.get(i);
            attribute.setValue(randomWords.next());
        }
    }

    private void deleteAttributes(String xpath) throws Exception {
        Nodes nodes = document.query(xpath);
        for (int i = 0; i < nodes.size(); i++) {
            Attribute attribute = (Attribute) nodes.get(i);
            attribute.detach();
        }
    }

    private void clearAttributesWithIpAddress(String xpath) throws Exception {
        Nodes nodes = document.query(xpath);
        for (int i = 0; i < nodes.size(); i++) {
            Attribute attribute = (Attribute) nodes.get(i);
            attribute.setValue(randomIpAddress());
        }
    }

    private String randomIpAddress() {
        return String.format("%d.%d.%d.%d", randomOctet(), randomOctet(), randomOctet(), randomOctet());
    }

    private int randomOctet() {
        return new Random().nextInt(253) + 1;
    }

    public void setRandomWords(RandomWords randomWords) {
        this.randomWords = randomWords;
    }
}
