import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
} from "@/components/ui/pagination";

interface CharacterPaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export function CharacterPagination({ currentPage, totalPages, onPageChange }: CharacterPaginationProps) {
  if (totalPages <= 1) {
    return null;
  }

  const handlePrevious = () => {
    if (currentPage > 0) {
      onPageChange(currentPage - 1);
    }
  };

  const handleNext = () => {
    if (currentPage < totalPages - 1) {
      onPageChange(currentPage + 1);
    }
  };

  return (
    <Pagination>
      <PaginationContent>
        <PaginationItem>
          <PaginationLink
            onClick={handlePrevious}
            aria-disabled={currentPage === 0}
            className={currentPage === 0 ? "pointer-events-none opacity-50 cursor-not-allowed" : "cursor-pointer"}
          >
            Previous
          </PaginationLink>
        </PaginationItem>
        {[...Array(totalPages)].map((_, index) => {
          const showEllipsis = Math.abs(currentPage - index) > 2;
          const isEllipsisBoundary = Math.abs(currentPage - index) === 3;

          if (showEllipsis && !isEllipsisBoundary) {
            return null; // Don't render numbers too far away
          }

          return (
            <PaginationItem key={index}>
              {isEllipsisBoundary ? (
                <PaginationEllipsis />
              ) : (
                <PaginationLink
                  onClick={() => onPageChange(index)}
                  isActive={currentPage === index}
                  className="cursor-pointer"
                >
                  {index + 1}
                </PaginationLink>
              )}
            </PaginationItem>
          );
        })}
        <PaginationItem>
          <PaginationLink
            onClick={handleNext}
            aria-disabled={currentPage === totalPages - 1}
            className={
              currentPage === totalPages - 1 ? "pointer-events-none opacity-50 cursor-not-allowed" : "cursor-pointer"
            }
          >
            Next
          </PaginationLink>
        </PaginationItem>
      </PaginationContent>
    </Pagination>
  );
}
