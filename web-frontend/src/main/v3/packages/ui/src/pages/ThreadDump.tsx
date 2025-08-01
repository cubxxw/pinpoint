import React from 'react';
import { SiDatabricks } from 'react-icons/si';
import { useSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import { MainHeader } from '../components/MainHeader';
import { ApplicationCombinedList } from '../components/Application';
import { ThreadDumpList } from '../components/ThreadDump/ThreadDumpList';
import { ResizableHandle, ResizablePanel, ResizablePanelGroup } from '../components/ui/resizable';
import { ThreadDumpDetail } from '../components/ThreadDump/ThreadDumpDetail';
import { ActiveThreadLightDump } from '@pinpoint-fe/ui/src/constants';

export const ThreadDumpPage = () => {
  const [selectedThread, setSelectedThread] =
    React.useState<ActiveThreadLightDump.ThreadDumpData>();
  const { application, searchParameters } = useSearchParameters();
  const agentId = searchParameters.agentId;

  return (
    <div className="flex flex-col flex-1 h-full">
      <MainHeader
        title={
          <div className="flex items-center gap-2">
            <SiDatabricks /> Thread Dump
          </div>
        }
      >
        <ApplicationCombinedList selectedApplication={application} disabled />
        {application && (
          <div className="flex items-center gap-1 ml-4 font-semibold truncate">
            <div className="truncate">({agentId})</div>
          </div>
        )}
      </MainHeader>
      <ResizablePanelGroup
        direction="vertical"
        // autoSaveId={APP_SETTING_KEYS.TRANSACTION_LIST_RESIZABLE}
      >
        <ResizablePanel minSize={10} maxSize={90}>
          <ThreadDumpList
            selectedThread={selectedThread}
            onClickRow={(rowData) => setSelectedThread(rowData.original)}
          />
        </ResizablePanel>
        <ResizableHandle withHandle />
        <ResizablePanel minSize={10} maxSize={90}>
          <ThreadDumpDetail thread={selectedThread} />
        </ResizablePanel>
      </ResizablePanelGroup>
    </div>
  );
};
