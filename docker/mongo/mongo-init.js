db = db.getSiblingDB("scene_orchestrator_db");

const sceneOrchestrations = db.getCollection("scene_orchestrations");

sceneOrchestrations.updateOne(
  { _id: BinData(3, "7u7u7u7u7u7u7u7u7u7u7g==") },
  {
    $setOnInsert: {
      _id: BinData(3, "7u7u7u7u7u7u7u7u7u7u7g=="),
      ownerUserId: BinData(3, "qqqqqqqqqqqqqqqqqqqqqg=="),
      automationSceneId: BinData(3, "zMzMzMzMzMzMzMzMzMzMzA=="),
      name: "Seed Evening Orchestration",
      status: "ACTIVE",
      createdAt: ISODate("2026-01-01T09:30:00.000Z"),
      deviceActions: [
        {
          deviceId: BinData(3, "EREREREREREREREREREREQ=="),
          actionType: "TURN_ON",
          parameters: "{\"brightness\":80}"
        }
      ],
      triggerRules: [
        {
          triggerId: "seed-evening-trigger",
          triggerType: "TIME_BASED",
          conditionExpression: "time == 18:00",
          enabled: true,
          triggerValue: "18:00"
        }
      ],
      _class: "org.chamsoft.smarthome.sceneorchestrator.domain.SceneOrchestration"
    }
  },
  { upsert: true }
);

sceneOrchestrations.updateOne(
  { _id: BinData(3, "/////////////////////w==") },
  {
    $setOnInsert: {
      _id: BinData(3, "/////////////////////w=="),
      ownerUserId: BinData(3, "qqqqqqqqqqqqqqqqqqqqqg=="),
      automationSceneId: BinData(3, "3d3d3d3d3d3d3d3d3d3d3Q=="),
      name: "Seed Morning Orchestration",
      status: "INACTIVE",
      createdAt: ISODate("2026-01-01T09:30:00.000Z"),
      deviceActions: [
        {
          deviceId: BinData(3, "EREREREREREREREREREREQ=="),
          actionType: "TURN_OFF",
          parameters: "{\"brightness\":0}"
        }
      ],
      triggerRules: [
        {
          triggerId: "seed-morning-trigger",
          triggerType: "TIME_BASED",
          conditionExpression: "time == 07:00",
          enabled: true,
          triggerValue: "07:00"
        }
      ],
      _class: "org.chamsoft.smarthome.sceneorchestrator.domain.SceneOrchestration"
    }
  },
  { upsert: true }
);
