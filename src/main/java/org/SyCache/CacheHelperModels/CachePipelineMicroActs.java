package org.SyCache.CacheHelperModels;


import org.SyCache.BaseNodeModel.Node;
import org.SyCache.CacheEntity.CacheInlineFunction;
import org.SyCache.Controllers.Cache;
import org.SyCache.DataNodeModels.StringNode;
import org.graalvm.polyglot.Value;
import java.util.HashMap;
import java.util.Vector;

public class CachePipelineMicroActs {
    private final String tmpWord = "tmp";
    private final String directWord = "dir";
    private final long sleepTimeForWatchCommand = 300;

    protected final Cache cache;

    public HashMap<String, microActFunctionPattern<String[], Vector<PipelineNodesHolder>, CachePipeline, Void>> hashmapActs;

    @FunctionalInterface
    public interface microActFunctionPattern<S, V, P, R> {
        public R apply(S s, V v, P p);
    }

    private void nodeAccessByPipeline(Node node) {
        node.accessNodeActions();
    }

    private boolean updateSnapShotNode(PipelineNodesHolder writePipelineNodesHolder, String value, Vector<PipelineNodesHolder> nodeStorage) {
        writePipelineNodesHolder.getNode2().updateNode(value);

        // Added To Storage For Commit Node //
        if (writePipelineNodesHolder.getNode1() != null) {
            nodeAccessByPipeline(writePipelineNodesHolder.getNode1());
            nodeStorage.add(writePipelineNodesHolder);
        }

        return true;
    }

    private PipelineNodesHolder recognizeNode(String microCodePart, String key, String value, Vector<PipelineNodesHolder> nodeStorage) throws CloneNotSupportedException {

        Node newNode = null; // Always Original Value
        Node lastNode = null; // Always new SnapShot Value

        PipelineNodesHolder pipelineNodesHolderRes = null;

        if (microCodePart.equals(tmpWord)) // if node is tmp micro
        {
            for (PipelineNodesHolder pipelineNodesHolder : nodeStorage) {
                if (pipelineNodesHolder.getNode2().getKey().equals(key))
                    pipelineNodesHolderRes = pipelineNodesHolder;
            }

            if (pipelineNodesHolderRes == null) {

                newNode = new StringNode(key, "");
                pipelineNodesHolderRes = new PipelineNodesHolder(null, newNode);
                nodeStorage.add(pipelineNodesHolderRes);
            }
        } else if (microCodePart.equals(directWord)) { // if value is direct //
            newNode = new StringNode(directWord, value);
            pipelineNodesHolderRes = new PipelineNodesHolder(null, newNode);
        } else { // at last old node and new cloned node
            lastNode = cache.getStorage().getPool(microCodePart).getItem(key);
            // Take Snapshot //
            newNode = (Node) lastNode.clone();

            pipelineNodesHolderRes = new PipelineNodesHolder(lastNode, newNode);
            pipelineNodesHolderRes.setPairNeedCommit(true);
        }


        // at all return pair
        return pipelineNodesHolderRes;
    }

    public CachePipelineMicroActs(Cache cache) {
        this.cache = cache;

        hashmapActs = new HashMap<>();

        //***MOV***//
        hashmapActs.put("MOV", (s, v, p) -> {
            // MOV , poolName:key1, poolName:key1 //
            // poolName could be [tmp] //
            // right poolName could be [dir]  it means you could insert any value direct //

            String[] part1 = s[1].split(":");
            String[] part2 = s[2].split(":");


            try {

                PipelineNodesHolder pipelineNodesHolderPart1 = recognizeNode(part1[0], part1[1], part1[1], v);

                PipelineNodesHolder pipelineNodesHolderPart2 = recognizeNode(part2[0], part2[1], part2[1], v);

                updateSnapShotNode(pipelineNodesHolderPart1, pipelineNodesHolderPart2.getNode2().deserializeNode(), v);

            } catch (Exception e) {
                CachePipelineMicroActsException exceptionMicro = new CachePipelineMicroActsException(e.getMessage(), String.join(",", s));
                throw new RuntimeException(exceptionMicro);
            }

            return null;
        });

        //***APP***//
        hashmapActs.put("APP", (s, v, p) -> {
            // APP , poolName:key1, value //
            // poolName could be [tmp] //

            String[] part1 = s[1].split(":");
            String value = s[2];

            try {

                PipelineNodesHolder pipelineNodesHolderPart1 = recognizeNode(part1[0], part1[1], part1[1], v);

                String data = pipelineNodesHolderPart1.getNode2().deserializeNode();
                updateSnapShotNode(pipelineNodesHolderPart1, data + value, v);

            } catch (Exception e) {
                CachePipelineMicroActsException exceptionMicro = new CachePipelineMicroActsException(e.getMessage(), String.join(",", s));
                throw new RuntimeException(exceptionMicro);
            }

            return null;
        });

        //***STR***//
        hashmapActs.put("SUB", (s, v, p) -> {
            // STR , poolName:key1, Start, End //
            // poolName could be [tmp] //

            String[] part1 = s[1].split(":");
            String start = s[2];
            String end = s[3];

            try {

                PipelineNodesHolder pipelineNodesHolderPart1 = recognizeNode(part1[0], part1[1], part1[1], v);

                String data = pipelineNodesHolderPart1.getNode2().deserializeNode();
                updateSnapShotNode(pipelineNodesHolderPart1, data.substring(Integer.parseInt(start), Integer.parseInt(end)), v);

            } catch (Exception e) {
                CachePipelineMicroActsException exceptionMicro = new CachePipelineMicroActsException(e.getMessage(), String.join(",", s));
                throw new RuntimeException(exceptionMicro);
            }

            return null;
        });


        //**INC***//
        hashmapActs.put("INC", (s, v, p) -> {
            // INC , poolName:key1,num //
            // poolName could be [tmp] //
            Vector<PipelineNodesHolder> nodes = v;

            String[] part1 = s[1].split(":");
            String num = s[2].isBlank() ? "1" : s[2];

            try {
                PipelineNodesHolder pipelineNodesHolderPart1 = recognizeNode(part1[0], part1[1], part1[1], v);

                long lData = Long.parseLong(pipelineNodesHolderPart1.getNode2().deserializeNode());
                lData += Long.parseLong(num);

                updateSnapShotNode(pipelineNodesHolderPart1, String.valueOf(lData), v);

            } catch (Exception e) {
                CachePipelineMicroActsException exceptionMicro = new CachePipelineMicroActsException(e.getMessage(), String.join(",", s));
                throw new RuntimeException(exceptionMicro);
            }

            return null;
        });

        //***MUL***//
        hashmapActs.put("MUL", (s, v, p) -> {
            // INC , poolName:key1,num //
            // poolName could be [tmp] //
            Vector<PipelineNodesHolder> nodes = v;

            String[] part1 = s[1].split(":");
            String num = s[2].isBlank() ? "1" : s[2];

            try {
                PipelineNodesHolder pipelineNodesHolderPart1 = recognizeNode(part1[0], part1[1], part1[1], v);

                long lData = Long.parseLong(pipelineNodesHolderPart1.getNode2().deserializeNode());
                lData *= Long.parseLong(num);

                updateSnapShotNode(pipelineNodesHolderPart1, String.valueOf(lData), v);

            } catch (Exception e) {
                CachePipelineMicroActsException exceptionMicro = new CachePipelineMicroActsException(e.getMessage(), String.join(",", s));
                throw new RuntimeException(exceptionMicro);
            }

            return null;
        });

        //***DEC***//
        hashmapActs.put("DEC", (s, v, p) -> {
            // INC , poolName:key1,num //
            // poolName could be [tmp] //

            String[] part1 = s[1].split(":");
            String num = s[2].isBlank() ? "1" : s[2];

            try {
                PipelineNodesHolder pipelineNodesHolderPart1 = recognizeNode(part1[0], part1[1], part1[1], v);

                long lData = Long.parseLong(pipelineNodesHolderPart1.getNode2().deserializeNode());
                lData -= Long.parseLong(num);

                updateSnapShotNode(pipelineNodesHolderPart1, String.valueOf(lData), v);

            } catch (Exception e) {
                CachePipelineMicroActsException exceptionMicro = new CachePipelineMicroActsException(e.getMessage(), String.join(",", s));
                throw new RuntimeException(exceptionMicro);
            }

            return null;
        });

        //***DIV***//
        hashmapActs.put("DIV", (s, v, p) -> {
            // INC , poolName:key1,num //
            // poolName could be [tmp] //

            String[] part1 = s[1].split(":");
            String num = s[2].isBlank() || s[2].equals("0") ? "1" : s[2];

            try {
                PipelineNodesHolder pipelineNodesHolderPart1 = recognizeNode(part1[0], part1[1], part1[1], v);

                long lData = Long.parseLong(pipelineNodesHolderPart1.getNode2().deserializeNode());
                lData /= Long.parseLong(num);

                updateSnapShotNode(pipelineNodesHolderPart1, String.valueOf(lData), v);

            } catch (Exception e) {
                CachePipelineMicroActsException exceptionMicro = new CachePipelineMicroActsException(e.getMessage(), String.join(",", s));
                throw new RuntimeException(exceptionMicro);
            }

            return null;
        });

        //***LEN***//
        hashmapActs.put("LEN", (s, v, p) -> {
            // LEN , poolName:key1, poolName:key2 //
            // poolName could be [tmp], len value of part2 going to part1 //

            String[] part1 = s[1].split(":");
            String[] part2 = s[2].split(":");

            try {

                PipelineNodesHolder pipelineNodesHolderPart1 = recognizeNode(part1[0], part1[1], part1[1], v);

                PipelineNodesHolder pipelineNodesHolderPart2 = recognizeNode(part2[0], part2[1], part2[1], v);

                updateSnapShotNode(pipelineNodesHolderPart1, String.valueOf(pipelineNodesHolderPart2.getNode2().deserializeNode().length()), v);

            } catch (Exception e) {
                CachePipelineMicroActsException exceptionMicro = new CachePipelineMicroActsException(e.getMessage(), String.join(",", s));
                throw new RuntimeException(exceptionMicro);
            }

            return null;
        });

        //***INV***//
        hashmapActs.put("INV", (s, v, p) -> {
            // INV , poolName:hashtag //

            String[] part1 = s[1].split(":");

            try {
                PipelineNodesHolder pipelineNodesHolder = new PipelineNodesHolder(null, null);
                pipelineNodesHolder.setActionApply(true);

                pipelineNodesHolder.getApplyInject().add((input) -> {
                    cache.getStorage().getPool(part1[0]).getTagValidateStorage().get(part1[1]).setValidate(false);
                    return null;
                });

                v.add(pipelineNodesHolder);

            } catch (Exception e) {
                CachePipelineMicroActsException exceptionMicro = new CachePipelineMicroActsException(e.getMessage(), String.join(",", s));
                throw new RuntimeException(exceptionMicro);
            }

            return null;
        });

        //***WHS***//
        hashmapActs.put("WHS", (s, v, p) -> {
            // WHS , poolName:key //

            String[] part1 = s[1].split(":");

            try {
                PipelineNodesHolder pipelineNodesHolderPart1 = recognizeNode(part1[0], part1[1], part1[1], v);
                pipelineNodesHolderPart1.setPairNeedCommit(false);

                // Take State of Watch Node //
                Node nextStateNode = pipelineNodesHolderPart1.getNode1().returnNexHistoryNodeLink();

                while (pipelineNodesHolderPart1.getNode1().returnNexHistoryNodeLink() == nextStateNode) {
                    Thread.sleep(sleepTimeForWatchCommand);
                }

            } catch (Exception e) {
                CachePipelineMicroActsException exceptionMicro = new CachePipelineMicroActsException(e.getMessage(), String.join(",", s));
                throw new RuntimeException(exceptionMicro);
            }

            return null;
        });

        //***WHV***//
        hashmapActs.put("WHV", (s, v, p) -> {
            // WHV , poolName:key, value //

            String[] part1 = s[1].split(":");

            try {

                PipelineNodesHolder pipelineNodesHolderPart1 = recognizeNode(part1[0], part1[1], part1[1], v);
                pipelineNodesHolderPart1.setPairNeedCommit(false);

                // Take Value of Watch Node //
                String value = pipelineNodesHolderPart1.getNode1().deserializeNode();

                //aware again fetch node data because maybe node has new state //
                while (!cache.getStorage().getPool(part1[0]).getItem(part1[1]).deserializeNode().equals(value)) {
                    Thread.sleep(sleepTimeForWatchCommand);
                }

            } catch (Exception e) {
                CachePipelineMicroActsException exceptionMicro = new CachePipelineMicroActsException(e.getMessage(), String.join(",", s));
                throw new RuntimeException(exceptionMicro);
            }

            return null;
        });

        //**LOP***//
        hashmapActs.put("LOP", (s, v, p) -> {
            // LOP , tmp:key //

            String[] part1 = s[1].split(":");


            try {
                PipelineNodesHolder pipelineNodesHolderPart1 = recognizeNode(part1[0], part1[1], part1[1], v);
                pipelineNodesHolderPart1.setPairNeedCommit(false);
                int counter = Integer.parseInt(pipelineNodesHolderPart1.getNode2().deserializeNode());

                PipelineNodesHolder pipelineNodesHolderControlStruct = new PipelineNodesHolder(null, null);
                pipelineNodesHolderControlStruct.setHolderStructure(true);

                if (counter > 1) {
                    pipelineNodesHolderControlStruct.setControlStructure(PipelineNodesHolderControlStructureEnum.JMP);
                    updateSnapShotNode(pipelineNodesHolderPart1, String.valueOf(--counter), v);
                }

                if (pipelineNodesHolderControlStruct.getControlStructure() != null)
                    v.add(pipelineNodesHolderControlStruct);

            } catch (Exception e) {
                CachePipelineMicroActsException exceptionMicro = new CachePipelineMicroActsException(e.getMessage(), String.join(",", s));
                throw new RuntimeException(exceptionMicro);
            }

            return null;
        });

        //***IFV***//
        hashmapActs.put("IFV", (s, v, p) -> {
            // IFV , poolName:key, control, value //
            // Control is [lt,lte,eq,gte,gt]

            String[] part1 = s[1].split(":");
            String control = s[2];
            String value = s[3];

            try {
                PipelineNodesHolder pipelineNodesHolderPart1 = recognizeNode(part1[0], part1[1], part1[1], v);
                PipelineNodesHolder pipelineNodesHolderControlStruct = new PipelineNodesHolder(null, null);
                pipelineNodesHolderControlStruct.setHolderStructure(true);

                //aware again fetch node data because maybe node has new state //
                switch (control) {
                    case "lt":
                        if (!(Long.parseLong(pipelineNodesHolderPart1.getNode2().deserializeNode()) < Long.parseLong(value))) {
                            pipelineNodesHolderControlStruct.setControlStructure(PipelineNodesHolderControlStructureEnum.IFVE);
                        }
                        break;
                    case "lte":
                        if (!(Long.parseLong(pipelineNodesHolderPart1.getNode2().deserializeNode()) <= Long.parseLong(value))) {
                            pipelineNodesHolderControlStruct.setControlStructure(PipelineNodesHolderControlStructureEnum.IFVE);
                        }
                        break;
                    case "eq":
                        if (!pipelineNodesHolderPart1.getNode2().deserializeNode().equals(value)) {
                            pipelineNodesHolderControlStruct.setControlStructure(PipelineNodesHolderControlStructureEnum.IFVE);
                        }
                        break;
                    case "gte":
                        if (!(Long.parseLong(pipelineNodesHolderPart1.getNode2().deserializeNode()) >= Long.parseLong(value))) {
                            pipelineNodesHolderControlStruct.setControlStructure(PipelineNodesHolderControlStructureEnum.IFVE);
                        }
                        break;
                    case "gt":
                        if (!(Long.parseLong(pipelineNodesHolderPart1.getNode2().deserializeNode()) > Long.parseLong(value))) {
                            pipelineNodesHolderControlStruct.setControlStructure(PipelineNodesHolderControlStructureEnum.IFVE);
                        }
                        break;
                }

                if (pipelineNodesHolderControlStruct.getControlStructure() != null)
                    v.add(pipelineNodesHolderControlStruct);

            } catch (Exception e) {
                CachePipelineMicroActsException exceptionMicro = new CachePipelineMicroActsException(e.getMessage(), String.join(",", s));
                throw new RuntimeException(exceptionMicro);
            }

            return null;
        });

        //***IFVE***//
        hashmapActs.put("IFVE", (s, v, p) -> {
            // IFVE //
            // End Of IFV Micro
            return null;
        });

        //***FORE***//
        hashmapActs.put("LOPE", (s, v, p) -> {
            // LOPE //
            // End Of FOR Micro //

            try {
                PipelineNodesHolder pipelineNodesHolderControlStruct = new PipelineNodesHolder(null, null);
                pipelineNodesHolderControlStruct.setHolderStructure(true);
                pipelineNodesHolderControlStruct.setControlStructure(PipelineNodesHolderControlStructureEnum.LOPE);

                v.add(pipelineNodesHolderControlStruct);
            } catch (Exception e) {
                CachePipelineMicroActsException exceptionMicro = new CachePipelineMicroActsException(e.getMessage(), String.join(",", s));
                throw new RuntimeException(exceptionMicro);
            }

            return null;
        });

        //***NOP***//
        hashmapActs.put("NOP", (s, v, p) -> {
            // NOP //
            // NOTHING
            return null;
        });

        //***ITV***//
        hashmapActs.put("ITV", (s, v, p) -> {
            // ITV , poolName:function, StreamName, Interrupt, IsImport //

            String[] part1 = s[1].split(":");
            String function = part1.length > 1 ? part1[1] : "";
            String streamName = s[2];
            boolean isInterrupt = Boolean.parseBoolean(s[3]);
            boolean isImport = Boolean.parseBoolean(s[4]);

            try {
                if (p.getPipelineType() != CachePipelineTypeEnum.ASYNC) {
                    throw new Exception("ITV just called in ASYNC Pattern");
                }

                Value func;
                CacheInlineFunction cacheInlineFunction;

                if (!function.isEmpty() && isImport) {
                    cacheInlineFunction = new CacheInlineFunction();
                    cacheInlineFunction.setName(streamName);
                    cacheInlineFunction.setFunction(function);

                    cache.getCacheStreamService().saveOrUpdate(cacheInlineFunction);
                } else if (!function.isEmpty()) {
                    cacheInlineFunction = new CacheInlineFunction();
                    cacheInlineFunction.setName(streamName);
                    cacheInlineFunction.setFunction(function);
                } else {
                    cacheInlineFunction = cache.getCacheStreamService().getCacheStreamByName(streamName);
                }

                p.setCacheInlineFunction(cacheInlineFunction);

                func = cacheInlineFunction.parse();

                cache.getStorage().getPool(part1[0]).forEach((c) -> {
                    Node node = (Node) c;

                    // Access Trigger //
                    nodeAccessByPipeline(node);

                    if (isInterrupt) {
                        node.updateNode(String.valueOf(func.execute(node.deserializeNode())));
                    } else {
                        String orgData = node.deserializeNode();
                        try {
                            // Send Copy Of Data For Change //
                            node.updateNode(String.valueOf(func.execute(orgData)));
                        } catch (Exception ex) {
                            // Add Exception And Recover Node Data
                            node.setLastNodeError(ex.getMessage());
                            node.serializeNode(orgData);
                        }
                    }
                });

            } catch (Exception e) {
                CachePipelineMicroActsException exceptionMicro = new CachePipelineMicroActsException(e.getMessage(), String.join(",", s));
                throw new RuntimeException(exceptionMicro);
            }

            return null;
        });


        //***STM***//
        hashmapActs.put("STM", (s, v, p) -> {
            // STM , poolName:function, StreamName, IsImport, IsUpdateNode, OutPutFunction //

            String[] part1 = s[1].split(":");
            String function = part1.length > 1 ? part1[1] : "";
            String streamName = s[2];
            boolean isImport = Boolean.parseBoolean(s[3]);
            boolean isNodeUpdate = Boolean.parseBoolean(s[4]);
            String output = s[5];

            try {

                if (p.getPipelineType() != CachePipelineTypeEnum.ASYNC) {
                    throw new Exception("STM just called in ASYNC Pattern");
                }

                CacheInlineFunction cacheInlineFunction = null;

                if (!function.isEmpty() && isImport) {
                    cacheInlineFunction = new CacheInlineFunction();
                    cacheInlineFunction.setName(streamName);
                    cacheInlineFunction.setFunction(function);

                    cache.getCacheStreamService().saveOrUpdate(cacheInlineFunction);
                } else if (!function.isEmpty()) {
                    cacheInlineFunction = new CacheInlineFunction();
                    cacheInlineFunction.setName(streamName);
                    cacheInlineFunction.setFunction(function);
                } else if (!streamName.isEmpty()) {
                    cacheInlineFunction = cache.getCacheStreamService().getCacheStreamByName(streamName);
                }

                p.setCacheInlineFunction(cacheInlineFunction);

                CacheStream cacheStreamChannel = new CacheStream(cacheInlineFunction, new CacheOutputDriver(CacheOutputTypeDriverEnum.valueOf(output.toUpperCase())), isNodeUpdate);

                p.setCacheStream(cacheStreamChannel);

                // Add Stream To Pool's Channel //
                cache.getStorage().getPool(part1[0]).addChannelToPool(cacheStreamChannel);

                // Run Cache Stream In Async Model //
                cacheStreamChannel.runInsideOfPipelineThread();

            } catch (Exception e) {
                CachePipelineMicroActsException exceptionMicro = new CachePipelineMicroActsException(e.getMessage(), String.join(",", s));
                throw new RuntimeException(exceptionMicro);
            }

            return null;
        });


    }
}
